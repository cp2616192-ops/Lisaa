package com.khanu.lisaa.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import java.util.*

class VoiceSpeaker(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var isInitializing = false

    init {
        isInitializing = true
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let {
                // Try US English (universal support)
                it.language = Locale.US
                it.setPitch(1.2f)  // Female Pitch
                it.setSpeechRate(0.9f) // Slow
                ready = true
                isInitializing = false

                // Set listener
                it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        // Speech finished
                    }
                    override fun onError(utteranceId: String?) {
                        // Speech error
                    }
                })
            }
        } else {
            isInitializing = false
            Toast.makeText(context, "TTS initialization failed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun speak(text: String): Boolean {
        if (ready) {
            val params = Bundle()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "LISAA_TTS")
            return true
        } else {
            // If not ready, try waiting for 1 second and retry once
            if (isInitializing) {
                Thread.sleep(1000) // Wait for initialization
                if (ready) {
                    val params = Bundle()
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "LISAA_TTS")
                    return true
                }
            }
            return false
        }
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
