package com.example.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CompanionPreferences
import com.example.data.local.CompanionSettings
import com.example.data.repository.AanyaChatRepository
import com.example.data.repository.CompanionVibe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "ChatViewModel"
private const val PRIMARY_STREAM_TIMEOUT_MS = 35_000L
private const val FALLBACK_TIMEOUT_MS = 25_000L

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
  private val repository: AanyaChatRepository,
  private val preferences: CompanionPreferences,
) : ViewModel() {

  private val activeSessionId = MutableStateFlow<Long?>(null)
  private val generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
  private val draftInput = MutableStateFlow("")
  private val isSettingsOpen = MutableStateFlow(false)

  private var activeStreamJob: Job? = null

  private val activeMessagesFlow = activeSessionId.flatMapLatest { sessionId ->
    if (sessionId == null) flowOf(emptyList()) else repository.observeMessages(sessionId)
  }

  val uiState: StateFlow<ChatUiState> = combine(
    repository.observeSessions(),
    activeSessionId,
    activeMessagesFlow,
    generationState,
    preferences.settingsFlow,
    draftInput,
    isSettingsOpen,
  ) { values ->
    @Suppress("UNCHECKED_CAST")
    val sessions = values[0] as List<com.example.data.local.ChatSessionEntity>
    val sessionId = values[1] as Long?
    @Suppress("UNCHECKED_CAST")
    val messages = values[2] as List<ChatMessageEntity>
    val genState = values[3] as GenerationState
    val settings = values[4] as CompanionSettings
    val draft = values[5] as String
    val settingsOpen = values[6] as Boolean

    ChatUiState(
      sessions = sessions,
      activeSessionId = sessionId,
      messages = messages,
      generationState = genState,
      settings = settings,
      draftInput = draft,
      isSettingsOpen = settingsOpen,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = ChatUiState(),
  )

  init {
    viewModelScope.launch {
      val initialId = repository.ensureActiveSession(CompanionVibe.WARM_BESTIE)
      activeSessionId.value = initialId
    }
  }

  fun onDraftChanged(newDraft: String) {
    draftInput.value = newDraft
  }

  fun openSettings() {
    isSettingsOpen.value = true
  }

  fun closeSettings() {
    isSettingsOpen.value = false
  }

  fun selectSession(sessionId: Long) {
    if (activeSessionId.value == sessionId) return
    stopStreaming()
    generationState.value = GenerationState.Idle
    activeSessionId.value = sessionId
  }

  fun startNewChat() {
    viewModelScope.launch {
      stopStreaming()
      generationState.value = GenerationState.Idle
      val currentMessages = uiState.value.messages
      val currentId = activeSessionId.value
      // If current session is already empty, reuse it cleanly
      if (currentId != null && currentMessages.isEmpty()) {
        return@launch
      }
      val newId = repository.createNewSession(uiState.value.settings.vibe)
      activeSessionId.value = newId
    }
  }

  fun deleteSession(sessionId: Long) {
    viewModelScope.launch {
      val wasActive = activeSessionId.value == sessionId
      if (wasActive) {
        stopStreaming()
        generationState.value = GenerationState.Idle
      }
      repository.deleteSession(sessionId)
      if (wasActive) {
        val nextId = repository.ensureActiveSession(uiState.value.settings.vibe)
        activeSessionId.value = nextId
      }
    }
  }

  fun clearActiveChat() {
    viewModelScope.launch {
      stopStreaming()
      generationState.value = GenerationState.Idle
      val sessionId = activeSessionId.value ?: return@launch
      repository.clearSessionMessages(sessionId)
    }
  }

  fun updateSettings(
    nickname: String,
    vibe: CompanionVibe,
    temperature: Float,
    darkThemeOverride: Boolean?,
  ) {
    viewModelScope.launch {
      preferences.updateSettings(
        nickname = nickname,
        vibe = vibe,
        temperature = temperature,
        darkThemeOverride = darkThemeOverride,
      )
      isSettingsOpen.value = false
    }
  }

  fun sendMessage(rawText: String = draftInput.value) {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty() || uiState.value.isBusy) return

    draftInput.value = ""

    activeStreamJob = viewModelScope.launch {
      val settings = uiState.value.settings
      val sessionId = activeSessionId.value
        ?: repository.ensureActiveSession(settings.vibe).also { activeSessionId.value = it }

      val priorMessages = repository.getMessagesSnapshot(sessionId)
      val isFirstNonError = priorMessages.none { !it.isError }

      repository.saveUserMessage(
        sessionId = sessionId,
        text = trimmed,
        isFirstMessage = isFirstNonError,
      )

      streamModelResponse(
        sessionId = sessionId,
        history = priorMessages,
        userPrompt = trimmed,
        settings = settings,
      )
    }
  }

  fun retryError(errorMessageId: Long) {
    if (uiState.value.isBusy) return

    activeStreamJob = viewModelScope.launch {
      val sessionId = activeSessionId.value ?: return@launch
      repository.deleteMessageById(errorMessageId)

      val snapshot = repository.getMessagesSnapshot(sessionId)
      val lastUser = snapshot.lastOrNull { it.role == "user" && !it.isError } ?: return@launch
      val historyBeforeLastUser = snapshot.filter { it.id != lastUser.id }

      streamModelResponse(
        sessionId = sessionId,
        history = historyBeforeLastUser,
        userPrompt = lastUser.content,
        settings = uiState.value.settings,
      )
    }
  }

  fun regenerateLastReply() {
    if (uiState.value.isBusy) return

    activeStreamJob = viewModelScope.launch {
      val sessionId = activeSessionId.value ?: return@launch
      val snapshot = repository.getMessagesSnapshot(sessionId)
      val lastMsg = snapshot.lastOrNull() ?: return@launch
      if (lastMsg.role == "model") {
        repository.deleteMessageById(lastMsg.id)
      }
      val updatedSnapshot = repository.getMessagesSnapshot(sessionId)
      val lastUser = updatedSnapshot.lastOrNull { it.role == "user" && !it.isError } ?: return@launch
      val historyBeforeLastUser = updatedSnapshot.filter { it.id != lastUser.id }

      streamModelResponse(
        sessionId = sessionId,
        history = historyBeforeLastUser,
        userPrompt = lastUser.content,
        settings = uiState.value.settings,
      )
    }
  }

  fun stopStreaming() {
    val currentState = generationState.value
    activeStreamJob?.cancel()
    activeStreamJob = null
    if (currentState is GenerationState.Streaming && currentState.partialText.isNotBlank()) {
      val sessionId = activeSessionId.value
      if (sessionId != null) {
        viewModelScope.launch {
          repository.saveModelMessage(sessionId, currentState.partialText.trim())
        }
      }
    }
    generationState.value = GenerationState.Idle
  }

  private suspend fun streamModelResponse(
    sessionId: Long,
    history: List<ChatMessageEntity>,
    userPrompt: String,
    settings: CompanionSettings,
  ) {
    generationState.value = GenerationState.WaitingForFirstToken
    val accumulated = StringBuilder()

    try {
      try {
        withTimeout(PRIMARY_STREAM_TIMEOUT_MS) {
          repository.streamReply(
            historyMessages = history,
            userMessage = userPrompt,
            nickname = settings.nickname,
            vibe = settings.vibe,
            temperature = settings.temperature,
          ).collect { chunk ->
            accumulated.append(chunk)
            generationState.value = GenerationState.Streaming(accumulated.toString())
          }
        }
      } catch (e: TimeoutCancellationException) {
        val partial = accumulated.toString().trim()
        if (partial.isNotBlank()) {
          throw e
        }
        Log.w(TAG, "Primary stream timed out after ${PRIMARY_STREAM_TIMEOUT_MS}ms; trying fast unary fallback", e)
        val fallbackReply = withTimeout(FALLBACK_TIMEOUT_MS) {
          repository.generateFastFallbackReply(
            historyMessages = history,
            userMessage = userPrompt,
            nickname = settings.nickname,
            vibe = settings.vibe,
            temperature = settings.temperature,
          )
        }
        if (!fallbackReply.isNullOrBlank()) {
          accumulated.append(fallbackReply)
        }
      }

      val finalReply = accumulated.toString().trim()
      if (finalReply.isNotBlank()) {
        repository.saveModelMessage(sessionId = sessionId, text = finalReply)
        generationState.value = GenerationState.Idle
      } else {
        val emptyMsg = "Aanya ka reply blank aaya. Ek baar aur try karo."
        repository.saveErrorMessage(sessionId, emptyMsg)
        generationState.value = GenerationState.Error(emptyMsg)
      }
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Aanya response timed out", e)
      val partial = accumulated.toString().trim()
      if (partial.isNotBlank()) {
        repository.saveModelMessage(sessionId, partial)
      }
      val timeoutMsg = "Response mein thoda zyada time lag gaya (60s). Please retry karo."
      repository.saveErrorMessage(sessionId, timeoutMsg)
      generationState.value = GenerationState.Error(timeoutMsg)
    } catch (e: CancellationException) {
      // Always rethrow CancellationException to preserve cooperative coroutine cancellation
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Firebase AI chat generation failed", e)
      val errorDetail = e.localizedMessage?.takeIf { it.isNotBlank() }
        ?: "Connection issue aa gaya. Internet aur Firebase setup check karke retry karo."
      repository.saveErrorMessage(sessionId, errorDetail)
      generationState.value = GenerationState.Error(errorDetail)
    }
  }

  class Factory(
    private val repository: AanyaChatRepository,
    private val preferences: CompanionPreferences,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
        return ChatViewModel(repository, preferences) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
