package com.lisaa.ai.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class SpeakerManager(
    context: Context,
    private val onFinished: () -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts.language = Locale.US
            tts.setPitch(1.2f)
            tts.setSpeechRate(0.9f)

            tts.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(id: String?) {
                        VoiceManager.setState(
                            AssistantState.SPEAKING
                        )
                    }

                    override fun onDone(id: String?) {

                        VoiceManager.setState(
                            AssistantState.WAITING
                        )

                        onFinished()
                    }

                    override fun onError(id: String?) {

                        VoiceManager.setState(
                            AssistantState.ERROR
                        )

                        onFinished()
                    }
                }
            )
        }
    }


    fun speak(text: String) {

        VoiceManager.setState(
            AssistantState.SPEAKING
        )

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "LISAA_REPLY"
        )
    }


    fun stop() {
        tts.stop()
    }


    fun destroy() {
        tts.shutdown()
    }
}
