package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.speech.SpeechRecognizer
import android.widget.Toast
import com.khanu.lisaa.memory.MemoryManager
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.personality.PersonalityEngine
import com.khanu.lisaa.voice.VoiceRecognizer
import com.khanu.lisaa.voice.VoiceSpeaker
import com.khanu.lisaa.wakeword.WakeWordEngine
import kotlinx.coroutines.*

class LisaaCoreService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var voiceSpeaker: VoiceSpeaker
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var memoryManager: MemoryManager
    private lateinit var wakeWordEngine: WakeWordEngine
    private lateinit var voiceRecognizer: VoiceRecognizer

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isAwaitingCommand = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        fun startService(context: Context) {
            val intent = Intent(context, LisaaCoreService::class.java)
            context.startForegroundService(intent)
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, LisaaCoreService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(applicationContext)
        voiceSpeaker = VoiceSpeaker(applicationContext)
        personalityEngine = PersonalityEngine()
        memoryManager = MemoryManager()
        personalityEngine.setPersonality(PersonalityEngine.PersonalityType.GF)

        // 1. Wake Word Engine
        wakeWordEngine = WakeWordEngine(applicationContext) {
            handleWakeWordDetected()
        }

        // 2. Voice Recognizer
        voiceRecognizer = VoiceRecognizer(
            applicationContext,
            onResult = { transcript ->
                handleUserSpeech(transcript)
            },
            onError = { errorCode ->
                if (errorCode != SpeechRecognizer.ERROR_NO_MATCH) {
                    // Silent fail, go back to wake word
                    isAwaitingCommand = false
                    wakeWordEngine.startListening()
                }
            }
        )

        startForeground(
            NOTIFICATION_ID,
            notificationHelper.createServiceNotification(
                title = "LISAA AI",
                content = "Listening for 'LISAA'...",
                showActions = false
            )
        )

        // Start the Wake Word loop
        wakeWordEngine.startListening()
        sendBroadcast(Intent("LISAA_STATE_CHANGE").putExtra("state", "LISTENING"))
        Toast.makeText(this, "LISAA is ready. Say 'LISAA'", Toast.LENGTH_LONG).show()
    }

    private fun handleWakeWordDetected() {
        if (isAwaitingCommand) return
        serviceScope.launch {
            // 1. Stop Wake Word (releases mic temporarily)
            wakeWordEngine.stopListening()
            isAwaitingCommand = true

            // 2. Give feedback (speak "Yes?")
            voiceSpeaker.speakWhenReady("Yes?")
            delay(300)

            // 3. Start Voice Recognizer for the actual command
            voiceRecognizer.startListening()

            // 4. Timeout: If user says nothing for 8 seconds, go back to wake word
            delay(8000)
            if (isAwaitingCommand) {
                voiceRecognizer.stopListening()
                isAwaitingCommand = false
                wakeWordEngine.startListening()
                Toast.makeText(this@LisaaCoreService, "Listening for 'LISAA'...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleUserSpeech(transcript: String) {
        if (!isAwaitingCommand) return
        serviceScope.launch {
            // 1. Stop Recognizer
            voiceRecognizer.stopListening()
            isAwaitingCommand = false

            // 2. Process via Personality + Memory
            memoryManager.remember(transcript)
            var response = personalityEngine.getResponse(transcript)

            // 3. Name check logic
            if (transcript.lowercase().contains("mera naam kya hai") || transcript.lowercase().contains("my name")) {
                val savedName = memoryManager.getShortMemory()
                    .findLast { it.text.startsWith("my name is", ignoreCase = true) }
                    ?.text?.replace("my name is", "")?.trim()
                if (!savedName.isNullOrEmpty()) {
                    response = "Your name is $savedName. I remember it."
                }
            }
            if (transcript.lowercase().startsWith("my name is")) {
                val name = transcript.substring(10).trim()
                memoryManager.remember("my name is $name", importance = 5)
                response = "Nice to meet you $name. I will remember your name."
            }

            // 4. Speak response
            voiceSpeaker.speakWhenReady(response)

            // 5. Wait a bit, then restart Wake Word
            delay(1500)
            wakeWordEngine.startListening()
            Toast.makeText(this@LisaaCoreService, "Listening for 'LISAA'...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        wakeWordEngine.destroy()
        voiceRecognizer.destroy()
        voiceSpeaker.shutdown()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? = null
}
