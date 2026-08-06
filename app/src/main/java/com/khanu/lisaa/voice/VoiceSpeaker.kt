package com.khanu.lisaa.voice

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.widget.Toast
import java.util.*

class VoiceSpeaker(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { ttsInstance ->
                // ✅ Step 1: Try to set the best female neural voice
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setBestFemaleVoice(ttsInstance)
                } else {
                    // Fallback for older Android
                    ttsInstance.language = Locale.US
                }

                // ✅ Step 2: Always apply cute settings (pitch & speed)
                ttsInstance.setPitch(1.2f)   // Higher pitch = cute/feminine
                ttsInstance.setSpeechRate(0.9f) // Slightly slower for natural feel

                ready = true

                ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {}
                })

                // Speak pending text if any
                pendingText?.let { speakNow(it) }
                pendingText = null
            }
        } else {
            Toast.makeText(context, "TTS init failed!", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun setBestFemaleVoice(ttsInstance: TextToSpeech) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return

        val voices = ttsInstance.voices ?: return
        if (voices.isEmpty()) {
            // No voices available, fallback to locale
            ttsInstance.language = Locale.US
            return
        }

        // Priority order: Indian Hindi > Indian English > US English
        val preferredLocales = listOf(
            Locale("hi", "IN"),   // Hindi India
            Locale("en", "IN"),   // English India
            Locale.US             // English US
        )

        // Try to find a Female + Neural + High Quality voice for each locale
        for (locale in preferredLocales) {
            val femaleVoice = voices.find { voice ->
                voice.locale == locale &&
                voice.name.contains("female", ignoreCase = true) &&
                (voice.quality == Voice.QUALITY_VERY_HIGH || voice.quality == Voice.QUALITY_HIGH)
            }
            if (femaleVoice != null) {
                ttsInstance.voice = femaleVoice
                return
            }
        }

        // If no female voice found, try just matching locale (any gender)
        for (locale in preferredLocales) {
            val voice = voices.find { it.locale == locale }
            if (voice != null) {
                ttsInstance.voice = voice
                return
            }
        }

        // Ultimate fallback: Use default locale
        ttsInstance.language = Locale.US
    }

    // Public method to speak
    fun speakWhenReady(text: String) {
        if (ready) {
            speakNow(text)
        } else {
            pendingText = text
            Toast.makeText(context, "TTS initializing...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakNow(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), "LISAA_TTS")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
