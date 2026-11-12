package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.repository.CompanionVibe
import com.example.ui.theme.OnlineGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val StarterPrompts = listOf(
  "Hi Aanya, kya kar rahi ho?",
  "Aaj thoda low feel ho raha hai...",
  "Chalo koi fun baat karte hain!",
  "Mujhe thoda motivate karo na",
)

@Composable
fun AanyaTopBar(
  vibe: CompanionVibe,
  isTyping: Boolean,
  showMenuButton: Boolean,
  onOpenDrawer: () -> Unit,
  onNewChat: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    tonalElevation = 2.dp,
  ) {
    Column(modifier = Modifier.statusBarsPadding()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (showMenuButton) {
          IconButton(
            onClick = onOpenDrawer,
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("history_drawer_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Menu,
              contentDescription = stringResource(R.string.history_cd),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        Box(modifier = Modifier.size(44.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_aanya_avatar),
            contentDescription = stringResource(R.string.aanya_avatar_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                shape = CircleShape,
              ),
          )
          Box(
            modifier = Modifier
              .size(12.dp)
              .align(Alignment.BottomEnd)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surface)
              .padding(2.dp)
              .clip(CircleShape)
              .background(OnlineGreen),
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = if (isTyping) {
              stringResource(R.string.aanya_subtitle_typing)
            } else {
              "${vibe.hinglishSubtitle} • Online"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (isTyping) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        IconButton(
          onClick = onNewChat,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("new_chat_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.AddComment,
            contentDescription = stringResource(R.string.new_chat_cd),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("settings_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = stringResource(R.string.settings_cd),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeEmptyState(
  nickname: String,
  vibe: CompanionVibe,
  onStarterSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Asymmetric layered hero artwork + overlapping Aanya portrait
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp),
      contentAlignment = Alignment.BottomCenter,
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 36.dp)
          .height(176.dp),
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
      ) {
        Box {
          Image(
            painter = painterResource(id = R.drawable.img_aanya_welcome),
            contentDescription = stringResource(R.string.welcome_banner_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.35f),
                  ),
                ),
              ),
          )
          Surface(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = vibe.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      Image(
        painter = painterResource(id = R.drawable.img_aanya_avatar),
        contentDescription = stringResource(R.string.aanya_avatar_cd),
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(84.dp)
          .clip(CircleShape)
          .border(
            width = 4.dp,
            color = MaterialTheme.colorScheme.background,
            shape = CircleShape,
          )
          .border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            shape = CircleShape,
          ),
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = stringResource(R.string.welcome_headline, nickname),
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = stringResource(R.string.welcome_subheadline),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .widthIn(max = 420.dp)
        .padding(horizontal = 8.dp),
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = stringResource(R.string.welcome_starters_title),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(12.dp))

    FlowRow(
      modifier = Modifier.widthIn(max = 520.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      StarterPrompts.forEachIndexed { index, prompt ->
        Surface(
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onStarterSelected(prompt) }
            .testTag("starter_chip_$index"),
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLow,
          border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
          ),
          tonalElevation = 1.dp,
        ) {
          Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun MessageBubble(
  message: ChatMessageEntity,
  isLastModelMessage: Boolean,
  isBusy: Boolean,
  onCopy: (String) -> Unit,
  onRetryError: (Long) -> Unit,
  onRegenerate: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isUser = message.role == "user"

  if (message.isError) {
    ErrorMessageBanner(
      errorText = message.content,
      onRetry = { onRetryError(message.id) },
      modifier = modifier,
    )
    return
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 5.dp),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Bottom,
  ) {
    if (!isUser) {
      Image(
        painter = painterResource(id = R.drawable.img_aanya_avatar),
        contentDescription = stringResource(R.string.aanya_avatar_cd),
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .padding(bottom = 18.dp)
          .size(30.dp)
          .clip(CircleShape)
          .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
      )
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 310.dp),
    ) {
      val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 22.dp, topEnd = 6.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
      } else {
        RoundedCornerShape(topStart = 6.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
      }

      if (isUser) {
        Box(
          modifier = Modifier
            .clip(bubbleShape)
            .background(
              Brush.linearGradient(
                colors = listOf(
                  MaterialTheme.colorScheme.primary,
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.86f),
                ),
              ),
            )
            .padding(horizontal = 16.dp, vertical = 11.dp),
        ) {
          Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
          )
        }
      } else {
        Surface(
          shape = bubbleShape,
          color = MaterialTheme.colorScheme.surfaceContainerLow,
          border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
          ),
          tonalElevation = 1.dp,
        ) {
          Text(
            text = parseSimpleMarkdown(message.content),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
          )
        }
      }

      Row(
        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = formatTime(message.timestamp),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        )

        if (!isUser) {
          IconButton(
            onClick = { onCopy(message.content) },
            modifier = Modifier
              .size(28.dp)
              .testTag("copy_message_${message.id}"),
          ) {
            Icon(
              imageVector = Icons.Rounded.ContentCopy,
              contentDescription = stringResource(R.string.copy_message_cd),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
              modifier = Modifier.size(14.dp),
            )
          }

          if (isLastModelMessage && !isBusy) {
            IconButton(
              onClick = onRegenerate,
              modifier = Modifier
                .size(28.dp)
                .testTag("regenerate_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = stringResource(R.string.regenerate_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.size(15.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun StreamingBubble(
  partialText: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 5.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.Bottom,
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_aanya_avatar),
      contentDescription = stringResource(R.string.aanya_avatar_cd),
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .size(30.dp)
        .clip(CircleShape)
        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
    )
    Spacer(modifier = Modifier.width(8.dp))

    Surface(
      shape = RoundedCornerShape(topStart = 6.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 22.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
      tonalElevation = 2.dp,
      modifier = Modifier.widthIn(max = 310.dp),
    ) {
      Text(
        text = parseSimpleMarkdown("$partialText ▍"),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
      )
    }
  }
}

@Composable
fun TypingIndicatorBubble(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "typing_dots")

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_aanya_avatar),
      contentDescription = stringResource(R.string.aanya_avatar_cd),
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .size(30.dp)
        .clip(CircleShape),
    )
    Spacer(modifier = Modifier.width(8.dp))

    Surface(
      shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        repeat(3) { index ->
          val offsetY by transition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
              animation = tween(durationMillis = 420, delayMillis = index * 130),
              repeatMode = RepeatMode.Reverse,
            ),
            label = "dot_$index",
          )
          Box(
            modifier = Modifier
              .size(7.dp)
              .offset(y = offsetY.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
          )
        }
      }
    }
  }
}

@Composable
fun ErrorMessageBanner(
  errorText: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Rounded.ErrorOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = errorText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.weight(1f),
      )
      Spacer(modifier = Modifier.width(8.dp))
      OutlinedButton(
        onClick = onRetry,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.5f)),
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .testTag("retry_button"),
      ) {
        Text(
          text = stringResource(R.string.retry_button),
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }
  }
}

@Composable
fun ChatComposer(
  value: String,
  isBusy: Boolean,
  onValueChange: (String) -> Unit,
  onSend: () -> Unit,
  onStop: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
    tonalElevation = 3.dp,
  ) {
    Column(
      modifier = Modifier
        .navigationBarsPadding()
        .imePadding()
        .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        ),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
              Text(
                text = stringResource(R.string.composer_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              color = MaterialTheme.colorScheme.onSurface,
            ),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
              capitalization = KeyboardCapitalization.Sentences,
              imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(
              onSend = {
                if (!isBusy && value.isNotBlank()) onSend()
              },
            ),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("chat_input_field"),
          )

          Spacer(modifier = Modifier.width(6.dp))

          if (isBusy) {
            FilledIconButton(
              onClick = onStop,
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
              ),
              modifier = Modifier
                .padding(bottom = 4.dp)
                .size(48.dp)
                .testTag("stop_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Stop,
                contentDescription = "Stop generating",
              )
            }
          } else {
            val canSend = value.isNotBlank()
            FilledIconButton(
              onClick = onSend,
              enabled = canSend,
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
              ),
              modifier = Modifier
                .padding(bottom = 4.dp)
                .size(48.dp)
                .testTag("send_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(R.string.send_message_cd),
              )
            }
          }
        }
      }
    }
  }
}

private fun formatTime(timestamp: Long): String {
  val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
  return formatter.format(Date(timestamp))
}

private fun parseSimpleMarkdown(input: String): AnnotatedString {
  return buildAnnotatedString {
    var i = 0
    while (i < input.length) {
      if (i + 1 < input.length && input[i] == '*' && input[i + 1] == '*') {
        val end = input.indexOf("**", i + 2)
        if (end != -1) {
          pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
          append(input.substring(i + 2, end))
          pop()
          i = end + 2
          continue
        }
      }
      if (input[i] == '*' && (i == 0 || input[i - 1] != '*')) {
        val end = input.indexOf('*', i + 1)
        if (end != -1 && end > i + 1) {
          pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
          append(input.substring(i + 1, end))
          pop()
          i = end + 1
          continue
        }
      }
      append(input[i])
      i++
    }
  }
}
