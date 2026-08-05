package com.khanu.lisaa.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (Int) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )
            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                1
            )
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                onError(error)
            }

            override fun onResults(results: Bundle?) {

                val list =
                    results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                if (!list.isNullOrEmpty()) {
                    onResult(list[0])
                }

            }

            override fun onPartialResults(partialResults: Bundle?) {

                val list =
                    partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                if (!list.isNullOrEmpty()) {
                    onResult(list[0])
                }

            }

            override fun onEvent(
                eventType: Int,
                params: Bundle?
            ) {
            }

        })

        speechRecognizer?.startListening(intent)

    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

}
