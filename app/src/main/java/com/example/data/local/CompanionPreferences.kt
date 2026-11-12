package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.repository.CompanionVibe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aanya_prefs")

data class CompanionSettings(
  val nickname: String = "Shona",
  val vibe: CompanionVibe = CompanionVibe.WARM_BESTIE,
  val temperature: Float = 0.7f,
  val darkThemeOverride: Boolean? = null,
)

class CompanionPreferences(private val context: Context) {
  private object Keys {
    val NICKNAME = stringPreferencesKey("nickname")
    val VIBE = stringPreferencesKey("vibe")
    val TEMPERATURE = floatPreferencesKey("temperature")
    val DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode") // "system", "light", "dark"
  }

  val settingsFlow: Flow<CompanionSettings> = context.dataStore.data.map { prefs ->
    val nickname = prefs[Keys.NICKNAME]?.takeIf { it.isNotBlank() } ?: "Shona"
    val vibe = CompanionVibe.fromKey(prefs[Keys.VIBE])
    val temperature = (prefs[Keys.TEMPERATURE] ?: 0.7f).coerceIn(0.2f, 1.2f)
    val darkOverride = when (prefs[Keys.DARK_THEME_MODE]) {
      "light" -> false
      "dark" -> true
      else -> null
    }
    CompanionSettings(
      nickname = nickname,
      vibe = vibe,
      temperature = temperature,
      darkThemeOverride = darkOverride,
    )
  }

  suspend fun updateSettings(
    nickname: String,
    vibe: CompanionVibe,
    temperature: Float,
    darkThemeOverride: Boolean?,
  ) {
    context.dataStore.edit { prefs ->
      prefs[Keys.NICKNAME] = nickname.trim().ifBlank { "Shona" }
      prefs[Keys.VIBE] = vibe.key
      prefs[Keys.TEMPERATURE] = temperature.coerceIn(0.2f, 1.2f)
      prefs[Keys.DARK_THEME_MODE] = when (darkThemeOverride) {
        false -> "light"
        true -> "dark"
        null -> "system"
      }
    }
  }
}
