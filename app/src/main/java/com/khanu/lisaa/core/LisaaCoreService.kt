package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import com.khanu.lisaa.memory.MemoryManager
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.personality.PersonalityEngine
import com.khanu.lisaa.voice.VoiceRecognizer
import com.khanu.lisaa.voice.VoiceSpeaker
import com.khanu.lisaa.wakeword.WakeWordEngine
import kotlinx.coroutines.*

class LisaaCoreService : Service() {

    private val TAG = "LisaaCoreService"
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
            val intent = Intent(context, LisaaCoreService::class.java)
            context.startForegroundService(intent)
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, LisaaCoreService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            // 1. Basic Helpers (Always work)
            notificationHelper = NotificationHelper(applicationContext)
            personalityEngine = PersonalityEngine()
            memoryManager = MemoryManager()
            personalityEngine.setPersonality(PersonalityEngine.PersonalityType.GF)

            // 2. Start Foreground (Must happen before heavy stuff)
            startForeground(
                NOTIFICATION_ID,
                notificationHelper.createServiceNotification(
                    title = "LISAA AI",
                    content = "Initializing...",
                    showActions = false
                )
            )
            Toast.makeText(this, "LISAA Initializing...", Toast.LENGTH_SHORT).show()

            // 3. Initialize VoiceSpeaker (TTS)
            try {
                voiceSpeaker = VoiceSpeaker(applicationContext)
                voiceSpeaker?.speakWhenReady("Hello, I am Lissa. Initializing.")
            } catch (e: Exception) {
                Log.e(TAG, "TTS init failed", e)
                Toast.makeText(this, "TTS Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

            // 4. Initialize WakeWordEngine (Microphone)
            try {
                wakeWordEngine = WakeWordEngine(applicationContext) {
                    handleWakeWordDetected()
                }
                val started = wakeWordEngine?.startListening() ?: false
                if (!started) {
                    Toast.makeText(this, "WakeWord: Mic failed! Check permission.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "WakeWord Active", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "WakeWord init failed", e)
                Toast.makeText(this, "WakeWord Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

            // 5. Initialize VoiceRecognizer
            try {
                voiceRecognizer = VoiceRecognizer(
                    applicationContext,
                    onResult = { transcript ->
                        handleUserSpeech(transcript)
                    },
                    onError = { errorCode ->
                        if (errorCode != SpeechRecognizer.ERROR_NO_MATCH) {
                            isAwaitingCommand = false
                            wakeWordEngine?.startListening()
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Recognizer init failed", e)
                Toast.makeText(this, "Recognizer Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

            isServiceReady = true
            sendBroadcast(Intent("LISAA_STATE_CHANGE").putExtra("state", "LISTENING"))
            Toast.makeText(this, "LISAA Ready. Say 'LISAA'", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e(TAG, "Service onCreate CRASHED", e)
            Toast.makeText(this, "Fatal Error: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf() // Stop service if critical init fails
        }
    }

    private fun handleWakeWordDetected() {
        if (!isServiceReady || isAwaitingCommand) return
        try {
            serviceScope.launch {
                isAwaitingCommand = true
                wakeWordEngine?.stopListening()

                // Speak feedback
                voiceSpeaker?.speakWhenReady("Yes?")
                delay(400)

                // Start recognizer
                voiceRecognizer?.startListening()

                // Timeout
                delay(8000)
                if (isAwaitingCommand) {
                    voiceRecognizer?.stopListening()
                    isAwaitingCommand = false
                    wakeWordEngine?.startListening()
                    Toast.makeText(this@LisaaCoreService, "Listening for 'LISAA'...", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "WakeWord handler error", e)
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

                // Name logic
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

                voiceSpeaker?.speakWhenReady(response)
                delay(1500)
                wakeWordEngine?.startListening()
                Toast.makeText(this@LisaaCoreService, "Listening for 'LISAA'...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "UserSpeech handler error", e)
            isAwaitingCommand = false
            wakeWordEngine?.startListening()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        wakeWordEngine?.destroy()
        voiceRecognizer?.destroy()
        voiceSpeaker?.shutdown()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? = null
}
