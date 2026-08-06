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
    private var voiceSpeaker: VoiceSpeaker? = null
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var memoryManager: MemoryManager
    private var wakeWordEngine: WakeWordEngine? = null
    private var voiceRecognizer: VoiceRecognizer? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isAwaitingCommand = false
    private var isServiceReady = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        fun startService(context: Context) {
            context.startForegroundService(Intent(context, LisaaCoreService::class.java))
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, LisaaCoreService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            notificationHelper = NotificationHelper(applicationContext)
            personalityEngine = PersonalityEngine()
            memoryManager = MemoryManager()
            personalityEngine.setPersonality(PersonalityEngine.PersonalityType.GF)

            // Notification
            startForeground(NOTIFICATION_ID, notificationHelper.createServiceNotification(
                title = "LISAA AI",
                content = "Initializing...",
                showActions = false
            ))

            // TTS
            try {
                voiceSpeaker = VoiceSpeaker(applicationContext)
                voiceSpeaker?.speakWhenReady("Hello, I am Lissa.")
            } catch (e: Exception) {
                Toast.makeText(this, "TTS Fail: ${e.message}", Toast.LENGTH_LONG).show()
            }

            // Wake Word
            try {
                wakeWordEngine = WakeWordEngine(applicationContext) { handleWakeWordDetected() }
                if (!wakeWordEngine!!.startListening()) {
                    Toast.makeText(this, "Mic failed! Check permission.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "WakeWord Active", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "WakeWord Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

            // Recognizer
            try {
                voiceRecognizer = VoiceRecognizer(
                    applicationContext,
                    onResult = { transcript -> handleUserSpeech(transcript) },
                    onError = { errorCode ->
                        if (errorCode != SpeechRecognizer.ERROR_NO_MATCH) {
                            isAwaitingCommand = false
                            wakeWordEngine?.startListening()
                        }
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Recognizer Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

            isServiceReady = true
            Toast.makeText(this, "LISAA Ready. Say 'LISAA'", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "FATAL: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun handleWakeWordDetected() {
        if (!isServiceReady || isAwaitingCommand) return
        try {
            serviceScope.launch {
                isAwaitingCommand = true
                wakeWordEngine?.stopListening()
                voiceSpeaker?.speakWhenReady("Yes?")
                delay(300)
                voiceRecognizer?.startListening()

                delay(8000)
                if (isAwaitingCommand) {
                    voiceRecognizer?.stopListening()
                    isAwaitingCommand = false
                    wakeWordEngine?.startListening()
                    Toast.makeText(this@LisaaCoreService, "Listening...", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "WakeWord Handler Error: ${e.message}", Toast.LENGTH_LONG).show()
            isAwaitingCommand = false
            wakeWordEngine?.startListening()
        }
    }

    private fun handleUserSpeech(transcript: String) {
        if (!isAwaitingCommand) return
        try {
            serviceScope.launch {
                voiceRecognizer?.stopListening()
                isAwaitingCommand = false

                memoryManager.remember(transcript)
                var response = personalityEngine.getResponse(transcript)

                if (transcript.lowercase().contains("mera naam kya hai") || transcript.lowercase().contains("my name")) {
                    val savedName = memoryManager.getShortMemory()
                        .findLast { it.text.startsWith("my name is", ignoreCase = true) }
                        ?.text?.replace("my name is", "")?.trim()
                    if (!savedName.isNullOrEmpty()) {
                        response = "Your name is $savedName."
                    }
                }
                if (transcript.lowercase().startsWith("my name is")) {
                    val name = transcript.substring(10).trim()
                    memoryManager.remember("my name is $name", importance = 5)
                    response = "Nice to meet you $name."
                }

                try {
                    voiceSpeaker?.speakWhenReady(response)
                } catch (e: Exception) {
                    Toast.makeText(this@LisaaCoreService, "Speak Fail: ${e.message}", Toast.LENGTH_LONG).show()
                }

                delay(1500)
                wakeWordEngine?.startListening()
                Toast.makeText(this@LisaaCoreService, "Listening...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "UserSpeech Error: ${e.message}", Toast.LENGTH_LONG).show()
            isAwaitingCommand = false
            wakeWordEngine?.startListening()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        wakeWordEngine?.destroy()
        voiceRecognizer?.destroy()
        voiceSpeaker?.shutdown()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? = null
}
