package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.data.local.AppDatabase
import com.example.data.local.CompanionPreferences
import com.example.data.repository.AanyaChatRepository
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.theme.AanyaTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // STEP 1: Configure Firebase App Check with launch intent before any UI or ViewModels
    configureAppCheck(this, intent)

    // STEP 2: Enable edge-to-edge display
    enableEdgeToEdge()

    val database = AppDatabase.getInstance(applicationContext)
    val repository = AanyaChatRepository(database.chatDao())
    val preferences = CompanionPreferences(applicationContext)

    // STEP 3: Render Compose UI
    setContent {
      val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(
          repository = repository,
          preferences = preferences,
        ),
      )
      val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()

      AanyaTheme(darkThemeOverride = uiState.settings.darkThemeOverride) {
        ChatScreen(
          uiState = uiState,
          onDraftChanged = chatViewModel::onDraftChanged,
          onSendMessage = chatViewModel::sendMessage,
          onStopStreaming = chatViewModel::stopStreaming,
          onRetryError = chatViewModel::retryError,
          onRegenerateLastReply = chatViewModel::regenerateLastReply,
          onSelectSession = chatViewModel::selectSession,
          onNewChat = chatViewModel::startNewChat,
          onDeleteSession = chatViewModel::deleteSession,
          onClearActiveChat = chatViewModel::clearActiveChat,
          onOpenSettings = chatViewModel::openSettings,
          onCloseSettings = chatViewModel::closeSettings,
          onSaveSettings = chatViewModel::updateSettings,
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    configureAppCheck(this, intent)
  }
}
