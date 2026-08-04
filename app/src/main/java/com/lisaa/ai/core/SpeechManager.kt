package com.lisaa.ai.core

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import java.util.Locale

class SpeechManager(private val activity: Activity) {

    companion object {
        const val REQUEST_CODE = 100
    }

    fun startListening() {

        VoiceManager.setState(AssistantState.LISTENING)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak to LISAA..."
        )

        activity.startActivityForResult(intent, REQUEST_CODE)
    }
}
