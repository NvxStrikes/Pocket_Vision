package com.pocketvision.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechAnnouncer(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    var isEnabled: Boolean = false
    private var lastSpokenText: String = ""
    private var lastSpokenTime: Long = 0L
    private val cooldownMs = 3500L

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    fun speak(text: String) {
        if (!isEnabled) return
        val now = System.currentTimeMillis()
        if (text == lastSpokenText && now - lastSpokenTime < cooldownMs) {
            return
        }
        lastSpokenText = text
        lastSpokenTime = now
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "POCKET_VISION_TTS")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
