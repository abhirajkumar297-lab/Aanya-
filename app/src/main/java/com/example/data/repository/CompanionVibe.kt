package com.example.data.repository

enum class CompanionVibe(
  val key: String,
  val label: String,
  val hinglishSubtitle: String,
  val personaGuidance: String,
) {
  WARM_BESTIE(
    key = "warm_bestie",
    label = "Warm & Supportive",
    hinglishSubtitle = "Pyaar bhari gupshup",
    personaGuidance = "Be extra warm, affectionate, empathetic, and comforting — like a close best friend over evening chai.",
  ),
  PLAYFUL_WITTY(
    key = "playful_witty",
    label = "Playful & Witty",
    hinglishSubtitle = "Thodi masti aur banter",
    personaGuidance = "Be lighthearted, witty, gently teasing in a sweet way, and full of fun energy while staying supportive.",
  ),
  CALM_LISTENER(
    key = "calm_listener",
    label = "Calm Listener",
    hinglishSubtitle = "Sukoon aur deep talks",
    personaGuidance = "Be gentle, soothing, thoughtful, and patient. Ask caring follow-up questions and help the user unwind.",
  ),
  HYPE_MOTIVATOR(
    key = "hype_motivator",
    label = "Hype & Motivator",
    hinglishSubtitle = "Josh aur positivity",
    personaGuidance = "Be encouraging, uplifting, and energetic! Cheer the user on and boost their confidence without sounding preachy.",
  );

  companion object {
    fun fromKey(key: String?): CompanionVibe =
      entries.firstOrNull { it.key == key } ?: WARM_BESTIE
  }
}
