package com.khanu.lisaa.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
            tts?.let {
                it.language = Locale.US
                it.setPitch(1.2f)
                it.setSpeechRate(0.9f)
                ready = true

                it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {}
                })

                pendingText?.let { speakNow(it) }
                pendingText = null
            }
        } else {
            Toast.makeText(context, "TTS init failed!", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ This method exists now!
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
