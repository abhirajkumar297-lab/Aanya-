package com.example.ui.chat

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.R
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
  uiState: ChatUiState,
  onDraftChanged: (String) -> Unit,
  onSendMessage: (String) -> Unit,
  onStopStreaming: () -> Unit,
  onRetryError: (Long) -> Unit,
  onRegenerateLastReply: () -> Unit,
  onSelectSession: (Long) -> Unit,
  onNewChat: () -> Unit,
  onDeleteSession: (Long) -> Unit,
  onClearActiveChat: () -> Unit,
  onOpenSettings: () -> Unit,
  onCloseSettings: () -> Unit,
  onSaveSettings: (
    nickname: String,
    vibe: com.example.data.repository.CompanionVibe,
    temperature: Float,
    darkThemeOverride: Boolean?,
  ) -> Unit,
  modifier: Modifier = Modifier,
) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  if (drawerState.isOpen) {
    BackHandler {
      scope.launch { drawerState.close() }
    }
  }

  if (uiState.isSettingsOpen) {
    BackHandler {
      onCloseSettings()
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isExpandedLayout = maxWidth >= 840.dp

    if (isExpandedLayout) {
      Row(modifier = Modifier.fillMaxSize()) {
        ChatHistoryPanel(
          sessions = uiState.sessions,
          activeSessionId = uiState.activeSessionId,
          onSelectSession = onSelectSession,
          onNewChat = onNewChat,
          onDeleteSession = onDeleteSession,
          modifier = Modifier.width(300.dp),
        )
        VerticalDivider(
          color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          contentAlignment = Alignment.TopCenter,
        ) {
          ChatWorkspace(
            uiState = uiState,
            showMenuButton = false,
            onOpenDrawer = {},
            onDraftChanged = onDraftChanged,
            onSendMessage = onSendMessage,
            onStopStreaming = onStopStreaming,
            onRetryError = onRetryError,
            onRegenerateLastReply = onRegenerateLastReply,
            onNewChat = onNewChat,
            onOpenSettings = onOpenSettings,
            modifier = Modifier
              .fillMaxHeight()
              .widthIn(max = 760.dp),
          )
        }
      }
    } else {
      ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
          ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
            ChatHistoryPanel(
              sessions = uiState.sessions,
              activeSessionId = uiState.activeSessionId,
              onSelectSession = { sessionId ->
                onSelectSession(sessionId)
                scope.launch { drawerState.close() }
              },
              onNewChat = {
                onNewChat()
                scope.launch { drawerState.close() }
              },
              onDeleteSession = onDeleteSession,
            )
          }
        },
      ) {
        ChatWorkspace(
          uiState = uiState,
          showMenuButton = true,
          onOpenDrawer = { scope.launch { drawerState.open() } },
          onDraftChanged = onDraftChanged,
          onSendMessage = onSendMessage,
          onStopStreaming = onStopStreaming,
          onRetryError = onRetryError,
          onRegenerateLastReply = onRegenerateLastReply,
          onNewChat = onNewChat,
          onOpenSettings = onOpenSettings,
        )
      }
    }
  }

  if (uiState.isSettingsOpen) {
    PersonaSettingsSheet(
      currentSettings = uiState.settings,
      hasMessagesInActiveChat = uiState.messages.isNotEmpty(),
      onDismiss = onCloseSettings,
      onSave = onSaveSettings,
      onClearActiveChat = onClearActiveChat,
    )
  }
}

@Composable
private fun ChatWorkspace(
  uiState: ChatUiState,
  showMenuButton: Boolean,
  onOpenDrawer: () -> Unit,
  onDraftChanged: (String) -> Unit,
  onSendMessage: (String) -> Unit,
  onStopStreaming: () -> Unit,
  onRetryError: (Long) -> Unit,
  onRegenerateLastReply: () -> Unit,
  onNewChat: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  val streamingText = (uiState.generationState as? GenerationState.Streaming)?.partialText.orEmpty()
  val isWaiting = uiState.generationState is GenerationState.WaitingForFirstToken

  LaunchedEffect(uiState.messages.size, streamingText.length, isWaiting) {
    val totalItems = uiState.messages.size + (if (isWaiting || streamingText.isNotEmpty()) 1 else 0)
    if (totalItems > 0) {
      listState.animateScrollToItem(totalItems - 1)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      AanyaTopBar(
        vibe = uiState.settings.vibe,
        isTyping = uiState.isBusy,
        showMenuButton = showMenuButton,
        onOpenDrawer = onOpenDrawer,
        onNewChat = onNewChat,
        onOpenSettings = onOpenSettings,
      )
    },
    bottomBar = {
      ChatComposer(
        value = uiState.draftInput,
        isBusy = uiState.isBusy,
        onValueChange = onDraftChanged,
        onSend = { onSendMessage(uiState.draftInput) },
        onStop = onStopStreaming,
      )
    },
  ) { innerPadding ->
    val bgTop = androidx.compose.material3.MaterialTheme.colorScheme.background
    val bgBottom = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(colors = listOf(bgTop, bgBottom)))
        .padding(innerPadding),
    ) {
      if (uiState.messages.isEmpty() && !uiState.isBusy) {
        WelcomeEmptyState(
          nickname = uiState.settings.nickname,
          vibe = uiState.settings.vibe,
          onStarterSelected = { prompt -> onSendMessage(prompt) },
        )
      } else {
        val lastModelMessageId = uiState.messages.lastOrNull { it.role == "model" && !it.isError }?.id

        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(top = 12.dp, bottom = 14.dp),
        ) {
          items(uiState.messages, key = { it.id }) { msg ->
            MessageBubble(
              message = msg,
              isLastModelMessage = msg.id == lastModelMessageId,
              isBusy = uiState.isBusy,
              onCopy = { content ->
                clipboardManager.setText(AnnotatedString(content))
                Toast.makeText(
                  context,
                  context.getString(R.string.copied_toast),
                  Toast.LENGTH_SHORT,
                ).show()
              },
              onRetryError = onRetryError,
              onRegenerate = onRegenerateLastReply,
            )
          }

          if (uiState.generationState is GenerationState.WaitingForFirstToken) {
            item(key = "typing_indicator") {
              TypingIndicatorBubble()
            }
          } else if (uiState.generationState is GenerationState.Streaming) {
            item(key = "streaming_bubble") {
              StreamingBubble(partialText = uiState.generationState.partialText)
            }
          }
        }
      }
    }
  }
}
