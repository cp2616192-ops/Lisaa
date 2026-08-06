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

            startForeground(NOTIFICATION_ID, notificationHelper.createServiceNotification(
                title = "LISAA AI",
                content = "Initializing...",
                showActions = false
            ))

            voiceSpeaker = VoiceSpeaker(applicationContext)
            voiceSpeaker?.speakWhenReady("Hello, I am Lissa.")

            wakeWordEngine = WakeWordEngine(applicationContext) { handleWakeWordDetected() }
            if (!wakeWordEngine!!.startListening()) {
                Toast.makeText(this, "Mic failed!", Toast.LENGTH_LONG).show()
            }

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
                // ----- STEP 1: Wake Word Stopped -----
                Toast.makeText(this@LisaaCoreService, "STEP 1: Wake Stopped", Toast.LENGTH_SHORT).show()
                isAwaitingCommand = true
                wakeWordEngine?.stopListening()
                delay(500) // Mic release time

                // ----- STEP 2: Speaking Yes -----
                Toast.makeText(this@LisaaCoreService, "STEP 2: Speaking Yes...", Toast.LENGTH_SHORT).show()
                try {
                    voiceSpeaker?.speakWhenReady("Yes?")
                } catch (e: Exception) {
                    Toast.makeText(this@LisaaCoreService, "Speak Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                // ----- STEP 3: Waiting for TTS to finish -----
                Toast.makeText(this@LisaaCoreService, "STEP 3: Waiting 2.5s...", Toast.LENGTH_SHORT).show()
                delay(2500)

                // ----- STEP 4: Starting Recognizer (THIS IS WHERE CRASH HAPPENS) -----
                Toast.makeText(this@LisaaCoreService, "STEP 4: Starting Mic...", Toast.LENGTH_SHORT).show()
                try {
                    val started = voiceRecognizer?.startListening() ?: false
                    if (started) {
                        Toast.makeText(this@LisaaCoreService, "Mic Started Success!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LisaaCoreService, "Mic Start Failed (false)", Toast.LENGTH_LONG).show()
                        isAwaitingCommand = false
                        wakeWordEngine?.startListening()
                        return@launch
                    }
                } catch (e: Exception) {
                    // ----- THIS WILL SHOW THE EXACT ERROR IF IT CRASHES HERE -----
                    Toast.makeText(this@LisaaCoreService, "CRASH: ${e.message}", Toast.LENGTH_LONG).show()
                    isAwaitingCommand = false
                    wakeWordEngine?.startListening()
                    return@launch
                }

                // ----- STEP 5: Timeout -----
                delay(8000)
                if (isAwaitingCommand) {
                    voiceRecognizer?.stopListening()
                    isAwaitingCommand = false
                    wakeWordEngine?.startListening()
                    Toast.makeText(this@LisaaCoreService, "Listening...", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Handler Crash: ${e.message}", Toast.LENGTH_LONG).show()
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

                voiceSpeaker?.speakWhenReady(response)
                delay(2000)
                wakeWordEngine?.startListening()
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
