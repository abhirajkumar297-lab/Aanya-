package com.example.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ChatSessionEntity
import com.example.data.local.CompanionSettings
import com.example.data.repository.CompanionVibe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryPanel(
  sessions: List<ChatSessionEntity>,
  activeSessionId: Long?,
  onSelectSession: (Long) -> Unit,
  onNewChat: () -> Unit,
  onDeleteSession: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxHeight()
      .background(MaterialTheme.colorScheme.surfaceContainerLowest)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 14.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_aanya_avatar),
        contentDescription = stringResource(R.string.aanya_avatar_cd),
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.chats_section_title),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "${sessions.size} conversations",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    FilledTonalButton(
      onClick = onNewChat,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("drawer_new_chat_button"),
      shape = RoundedCornerShape(16.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Add,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.new_chat_label),
        style = MaterialTheme.typography.labelLarge,
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (sessions.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(R.string.no_saved_chats),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
      ) {
        items(sessions, key = { it.id }) { session ->
          val isActive = session.id == activeSessionId
          val vibe = CompanionVibe.fromKey(session.vibeKey)

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize()
              .clip(RoundedCornerShape(16.dp))
              .clickable { onSelectSession(session.id) }
              .testTag("session_item_${session.id}"),
            shape = RoundedCornerShape(16.dp),
            color = if (isActive) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
            } else {
              MaterialTheme.colorScheme.surfaceContainerLow
            },
            border = if (isActive) {
              BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            } else {
              null
            },
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = null,
                tint = if (isActive) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = session.title,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                  ),
                  color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${vibe.label} • ${formatSessionDate(session.updatedAt)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              IconButton(
                onClick = { onDeleteSession(session.id) },
                modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("delete_session_${session.id}"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.DeleteOutline,
                  contentDescription = stringResource(R.string.delete_chat_cd),
                  tint = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                  },
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaSettingsSheet(
  currentSettings: CompanionSettings,
  hasMessagesInActiveChat: Boolean,
  onDismiss: () -> Unit,
  onSave: (nickname: String, vibe: CompanionVibe, temperature: Float, darkThemeOverride: Boolean?) -> Unit,
  onClearActiveChat: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var nickname by remember(currentSettings) { mutableStateOf(currentSettings.nickname) }
  var selectedVibe by remember(currentSettings) { mutableStateOf(currentSettings.vibe) }
  var temperature by remember(currentSettings) { mutableFloatStateOf(currentSettings.temperature) }
  var darkOverride by remember(currentSettings) {
    mutableStateOf(currentSettings.darkThemeOverride)
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
    ) {
      Text(
        text = stringResource(R.string.persona_sheet_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = nickname,
        onValueChange = { if (it.length <= 24) nickname = it },
        label = { Text(stringResource(R.string.nickname_label)) },
        placeholder = { Text(stringResource(R.string.nickname_hint)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("nickname_input"),
      )

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = stringResource(R.string.vibe_section_label),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CompanionVibe.entries.forEach { vibe ->
          val selected = vibe == selectedVibe
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize()
              .clip(RoundedCornerShape(16.dp))
              .clickable { selectedVibe = vibe }
              .testTag("vibe_option_${vibe.key}"),
            shape = RoundedCornerShape(16.dp),
            color = if (selected) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceContainerLow
            },
            border = BorderStroke(
              width = 1.dp,
              color = if (selected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
              },
            ),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = vibe.label,
                  style = MaterialTheme.typography.labelLarge,
                  color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                )
                Text(
                  text = vibe.hinglishSubtitle,
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = stringResource(R.string.temperature_label, temperature),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Slider(
        value = temperature,
        onValueChange = { temperature = (Math.round(it * 10f) / 10f).coerceIn(0.2f, 1.2f) },
        valueRange = 0.2f..1.2f,
        steps = 9,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("temperature_slider"),
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = stringResource(R.string.theme_section_label),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier = Modifier.height(8.dp))

      val themeOptions = listOf(
        null to stringResource(R.string.theme_system),
        false to stringResource(R.string.theme_light),
        true to stringResource(R.string.theme_dark),
      )

      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        themeOptions.forEachIndexed { index, (value, label) ->
          SegmentedButton(
            selected = darkOverride == value,
            onClick = { darkOverride = value },
            shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("theme_option_$index"),
          ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
          }
        }
      }

      if (hasMessagesInActiveChat) {
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(
          onClick = {
            onClearActiveChat()
            onDismiss()
          },
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("clear_chat_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteSweep,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.clear_chat_button))
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { onSave(nickname, selectedVibe, temperature, darkOverride) },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_settings_button"),
      ) {
        Text(
          text = stringResource(R.string.save_settings_button),
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}

private fun formatSessionDate(timestamp: Long): String {
  val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
  return formatter.format(Date(timestamp))
}
