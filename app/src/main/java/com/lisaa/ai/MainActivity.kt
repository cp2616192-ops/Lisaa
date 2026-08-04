package com.lisaa.ai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lisaa.ai.core.*
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var btnMic: Button
    private lateinit var txtResult: TextView

    private lateinit var speechManager: SpeechManager
    private lateinit var speakerManager: SpeakerManager
    private lateinit var assistantController: AssistantController
    private lateinit var audioFocusManager: AudioFocusManager
    private lateinit var lisaaBrain: LisaaBrain


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        btnMic = findViewById(R.id.btnMic)
        txtResult = findViewById(R.id.txtResult)

        assistantController = AssistantController(this)
        audioFocusManager = AudioFocusManager()
        lisaaBrain = LisaaBrain()

        speakerManager = SpeakerManager(this) {

    audioFocusManager.enableMic()

    assistantController.idle()

    txtResult.text = "Listening..."

    speechManager.startListening()
}


        

            

            

            
           


        btnMic.setOnClickListener {

            audioFocusManager.enableMic()

            assistantController.startListening()

            speechManager.startListening()
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )


        if (
            requestCode == SpeechManager.REQUEST_CODE &&
            resultCode == Activity.RESULT_OK
        ) {

            val result =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )


            if (!result.isNullOrEmpty()) {

                val text = result[0]

                txtResult.text = text


                if (WakeWordManager.detected(text)) {

                    ConversationQueue.add(text)

                    assistantController.startThinking()

                    audioFocusManager.enableSpeaker()


                    val response =
                        lisaaBrain.process(text)


                    speakerManager.speak(response)


                } else {

                    txtResult.text =
                        "Say Hello LISAA"
                }
            }
        }
    }


    override fun onDestroy() {

        super.onDestroy()

        speakerManager.destroy()
    }
}
