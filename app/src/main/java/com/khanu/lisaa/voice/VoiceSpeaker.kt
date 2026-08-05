package com.khanu.lisaa.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceSpeaker(
    private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false

    var onSpeechFinished: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            ready = true

            tts?.language = Locale("en", "IN")
            tts?.setPitch(1.1f)
            tts?.setSpeechRate(1.0f)

            tts?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        onSpeechFinished?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        onSpeechFinished?.invoke()
                    }

                }
            )

        }

    }

    fun speak(text: String) {

        if (!ready) return

        val params = Bundle()

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            params,
            "LISAA_TTS"
        )

    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }

}
