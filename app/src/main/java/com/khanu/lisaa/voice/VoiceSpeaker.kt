package com.khanu.lisaa.voice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import android.widget.Toast
import java.util.*

class VoiceSpeaker(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "VoiceSpeaker"
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingText: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            showToast("TTS Init Error: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        try {
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { ttsInstance ->
                    try {
                        // Voice Selection (with error handling)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            val voices = ttsInstance.voices
                            if (voices != null && voices.isNotEmpty()) {
                                val preferredLocales = listOf(
                                    Locale("hi", "IN"),
                                    Locale("en", "IN"),
                                    Locale.US
                                )
                                var voiceSet = false
                                for (locale in preferredLocales) {
                                    val femaleVoice = voices.find { voice ->
                                        voice.locale == locale &&
                                                voice.name.contains("female", ignoreCase = true) &&
                                                (voice.quality == Voice.QUALITY_VERY_HIGH || voice.quality == Voice.QUALITY_HIGH)
                                    }
                                    if (femaleVoice != null) {
                                        ttsInstance.voice = femaleVoice
                                        voiceSet = true
                                        break
                                    }
                                }
                                if (!voiceSet) {
                                    for (locale in preferredLocales) {
                                        val voice = voices.find { it.locale == locale }
                                        if (voice != null) {
                                            ttsInstance.voice = voice
                                            break
                                        }
                                    }
                                }
                            }
                        } else {
                            ttsInstance.language = Locale.US
                        }

                        ttsInstance.setPitch(1.2f)
                        ttsInstance.setSpeechRate(0.9f)

                        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {}
                            override fun onDone(utteranceId: String?) {}
                            override fun onError(utteranceId: String?) {
                                showToast("TTS Utterance Error")
                            }
                        })

                        ready = true
                        pendingText?.let { speakNow(it) }
                        pendingText = null
                    } catch (e: Exception) {
                        showToast("TTS Setup Error: ${e.message}")
                        ready = false
                    }
                }
            } else {
                showToast("TTS Init Failed (status=$status)")
            }
        } catch (e: Exception) {
            showToast("TTS onInit Crash: ${e.message}")
        }
    }

    fun speakWhenReady(text: String) {
        mainHandler.post {
            try {
                if (ready && tts != null) {
                    speakNow(text)
                } else {
                    pendingText = text
                    showToast("TTS Busy, queuing...")
                }
            } catch (e: Exception) {
                showToast("Speak Error: ${e.message}")
            }
        }
    }

    private fun speakNow(text: String) {
        try {
            if (tts == null) {
                showToast("TTS is null, reinitializing...")
                tts = TextToSpeech(context, this)
                return
            }
            if (!ready) {
                showToast("TTS not ready, retrying...")
                pendingText = text
                return
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), "LISAA_TTS")
        } catch (e: Exception) {
            showToast("SpeakNow Crash: ${e.message}")
            // Try to recover
            try {
                tts?.shutdown()
                tts = TextToSpeech(context, this)
            } catch (e2: Exception) {
                showToast("Recovery Failed: ${e2.message}")
            }
        }
    }

    private fun showToast(msg: String) {
        mainHandler.post {
            Toast.makeText(context, "🔊 $msg", Toast.LENGTH_LONG).show()
            Log.e(TAG, msg)
        }
    }

    fun stop() {
        try { tts?.stop() } catch (e: Exception) { /* ignore */ }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) { /* ignore */ }
    }
}
