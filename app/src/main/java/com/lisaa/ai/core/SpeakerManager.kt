package com.lisaa.ai.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeakerManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setPitch(1.2f)
            tts.setSpeechRate(0.9f)
        }
    }

    fun speak(text: String) {

        VoiceManager.setState(AssistantState.SPEAKING)

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "LISAA"
        )
    }

    fun stop() {
        tts.stop()
    }

    fun destroy() {
        tts.shutdown()
    }
}
