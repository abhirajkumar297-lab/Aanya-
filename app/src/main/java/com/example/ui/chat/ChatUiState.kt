package com.example.ui.chat

import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.CompanionSettings

sealed interface GenerationState {
  data object Idle : GenerationState
  data object WaitingForFirstToken : GenerationState
  data class Streaming(val partialText: String) : GenerationState
  data class Error(val message: String) : GenerationState
}

data class ChatUiState(
  val sessions: List<ChatSessionEntity> = emptyList(),
  val activeSessionId: Long? = null,
  val messages: List<ChatMessageEntity> = emptyList(),
  val generationState: GenerationState = GenerationState.Idle,
  val settings: CompanionSettings = CompanionSettings(),
  val draftInput: String = "",
  val isSettingsOpen: Boolean = false,
) {
  val isBusy: Boolean
    get() = generationState is GenerationState.WaitingForFirstToken ||
      generationState is GenerationState.Streaming
}
