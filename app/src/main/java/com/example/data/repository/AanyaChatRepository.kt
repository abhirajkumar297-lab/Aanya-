package com.example.data.repository

import android.util.Log
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

private const val TAG = "AanyaChatRepository"

class AanyaChatRepository(
  private val chatDao: ChatDao,
) {
  companion object {
    const val MODEL_NAME = "gemini-3.8-flash"
    const val FAST_FALLBACK_MODEL_NAME = "gemini-3.5-flash-lite"
    const val DEFAULT_SESSION_TITLE = "Nayi Baat"
  }

  // Lazy default model ensures Firebase.ai is never evaluated eagerly at construction time
  private val defaultModel by lazy {
    createGenerativeModel(
      modelName = MODEL_NAME,
      nickname = "Shona",
      vibe = CompanionVibe.WARM_BESTIE,
      temperature = 0.7f,
      timeoutSeconds = 35,
    )
  }

  fun observeSessions(): Flow<List<ChatSessionEntity>> = chatDao.observeSessions()

  fun observeMessages(sessionId: Long): Flow<List<ChatMessageEntity>> =
    chatDao.observeMessages(sessionId)

  suspend fun ensureActiveSession(vibe: CompanionVibe): Long {
    val recent = chatDao.getMostRecentSession()
    if (recent != null) return recent.id
    return createNewSession(vibe)
  }

  suspend fun createNewSession(vibe: CompanionVibe): Long {
    val now = System.currentTimeMillis()
    return chatDao.insertSession(
      ChatSessionEntity(
        title = DEFAULT_SESSION_TITLE,
        vibeKey = vibe.key,
        createdAt = now,
        updatedAt = now,
      )
    )
  }

  suspend fun deleteSession(sessionId: Long) {
    chatDao.deleteSession(sessionId)
  }

  suspend fun clearSessionMessages(sessionId: Long) {
    chatDao.deleteMessagesForSession(sessionId)
    chatDao.updateSessionTitleAndTime(
      sessionId = sessionId,
      title = DEFAULT_SESSION_TITLE,
      updatedAt = System.currentTimeMillis(),
    )
  }

  suspend fun saveUserMessage(sessionId: Long, text: String, isFirstMessage: Boolean): Long {
    val now = System.currentTimeMillis()
    val id = chatDao.insertMessage(
      ChatMessageEntity(
        sessionId = sessionId,
        role = "user",
        content = text,
        timestamp = now,
      )
    )
    if (isFirstMessage) {
      val snippet = text.trim().replace("\n", " ").take(36)
      val title = if (text.trim().length > 36) "$snippet…" else snippet
      chatDao.updateSessionTitleAndTime(sessionId, title.ifBlank { DEFAULT_SESSION_TITLE }, now)
    } else {
      chatDao.updateSessionTime(sessionId, now)
    }
    return id
  }

  suspend fun saveModelMessage(sessionId: Long, text: String): Long {
    val now = System.currentTimeMillis()
    val id = chatDao.insertMessage(
      ChatMessageEntity(
        sessionId = sessionId,
        role = "model",
        content = text,
        timestamp = now,
      )
    )
    chatDao.updateSessionTime(sessionId, now)
    return id
  }

  suspend fun saveErrorMessage(sessionId: Long, errorText: String): Long {
    return chatDao.insertMessage(
      ChatMessageEntity(
        sessionId = sessionId,
        role = "model",
        content = errorText,
        timestamp = System.currentTimeMillis(),
        isError = true,
      )
    )
  }

  suspend fun deleteMessageById(messageId: Long) {
    chatDao.deleteMessageById(messageId)
  }

  suspend fun getMessagesSnapshot(sessionId: Long): List<ChatMessageEntity> =
    chatDao.getMessagesForSession(sessionId)

  fun streamReply(
    historyMessages: List<ChatMessageEntity>,
    userMessage: String,
    nickname: String,
    vibe: CompanionVibe,
    temperature: Float,
  ): Flow<String> = flow {
    Log.d(TAG, "Starting streamReply with model=$MODEL_NAME")
    val model = if (nickname == "Shona" && vibe == CompanionVibe.WARM_BESTIE && temperature == 0.7f) {
      defaultModel
    } else {
      createGenerativeModel(
        modelName = MODEL_NAME,
        nickname = nickname,
        vibe = vibe,
        temperature = temperature,
        timeoutSeconds = 35,
      )
    }

    val validHistory = mapHistory(historyMessages)
    val chat = model.startChat(history = validHistory)
    var firstChunkLogged = false
    chat.sendMessageStream(userMessage).collect { chunk ->
      val piece = chunk.text
      if (!piece.isNullOrEmpty()) {
        if (!firstChunkLogged) {
          firstChunkLogged = true
          Log.d(TAG, "First streamed chunk received from $MODEL_NAME")
        }
        emit(piece)
      }
    }
  }.flowOn(Dispatchers.IO)

  suspend fun generateFastFallbackReply(
    historyMessages: List<ChatMessageEntity>,
    userMessage: String,
    nickname: String,
    vibe: CompanionVibe,
    temperature: Float,
  ): String? = withContext(Dispatchers.IO) {
    Log.d(TAG, "Running unary fallback with model=$FAST_FALLBACK_MODEL_NAME")
    val model = createGenerativeModel(
      modelName = FAST_FALLBACK_MODEL_NAME,
      nickname = nickname,
      vibe = vibe,
      temperature = temperature,
      timeoutSeconds = 25,
    )
    val chat = model.startChat(history = mapHistory(historyMessages))
    chat.sendMessage(userMessage).text
  }

  private fun mapHistory(historyMessages: List<ChatMessageEntity>) =
    historyMessages
      .filter { !it.isError && it.content.isNotBlank() }
      .map { msg ->
        content(role = msg.role) {
          text(msg.content)
        }
      }

  private fun createGenerativeModel(
    modelName: String,
    nickname: String,
    vibe: CompanionVibe,
    temperature: Float,
    timeoutSeconds: Int,
  ): GenerativeModel {
    val safeNickname = nickname.trim().ifBlank { "Shona" }
    val systemPrompt = """
      You are Aanya, a friendly conversational companion.
      Speak in casual Hinglish, be supportive, and call the user '$safeNickname'.
      Keep your replies natural, expressive, and conversational like texting a close friend on WhatsApp — not robotic or overly formal.
      ${vibe.personaGuidance}
      Use natural Hinglish (Roman Hindi + everyday English words seamlessly mixed), occasional warm emojis when fitting, and keep responses concise unless the user asks for something detailed.
    """.trimIndent()

    return Firebase.ai.generativeModel(
      modelName = modelName,
      generationConfig = generationConfig {
        this.temperature = temperature
        if (modelName == MODEL_NAME) {
          thinkingConfig = thinkingConfig {
            thinkingLevel = ThinkingLevel.LOW
          }
        }
      },
      systemInstruction = content {
        text(systemPrompt)
      },
    )
  }
}
