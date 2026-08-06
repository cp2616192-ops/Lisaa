package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.khanu.lisaa.memory.MemoryManager
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.personality.PersonalityEngine
import com.khanu.lisaa.voice.VoiceSpeaker

class LisaaCoreService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var voiceSpeaker: VoiceSpeaker
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var memoryManager: MemoryManager

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
        memoryManager = MemoryManager() // ✅ Memory Added

        // Set personality to GF mode for testing
        personalityEngine.setPersonality(PersonalityEngine.PersonalityType.GF)

        val notification = notificationHelper.createServiceNotification(
            title = "LISAA AI",
            content = "Memory + Personality Active",
            showActions = false
        )
        startForeground(NOTIFICATION_ID, notification)

        // Speak welcome message (using "Lissa" for better pronunciation)
        voiceSpeaker.speakWhenReady("Hello, I am Lissa. I am ready to talk.")

        sendBroadcast(Intent("LISAA_STATE_CHANGE").putExtra("state", "LISTENING"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("TEST_COMMAND")?.let { command ->
            handleTestCommand(command)
        }
        return START_STICKY
    }

    private fun handleTestCommand(command: String) {
        // 1. Save user input to memory
        memoryManager.remember(command)

        // 2. Get response from Personality Engine
        var response = personalityEngine.getResponse(command)

        // 3. Check if user is asking about their name
        if (command.lowercase().contains("mera naam kya hai") || command.lowercase().contains("my name")) {
            val savedName = memoryManager.getShortMemory()
                .findLast { it.text.startsWith("my name is", ignoreCase = true) }
                ?.text?.replace("my name is", "")?.trim()
            if (!savedName.isNullOrEmpty()) {
                response = "Your name is $savedName. I remember it."
            }
        }

        // 4. If user says "my name is X", save it and confirm
        if (command.lowercase().startsWith("my name is")) {
            val name = command.substring(10).trim()
            memoryManager.remember("my name is $name", importance = 5)
            response = "Nice to meet you $name. I will remember your name."
        }

        // 5. Speak and show toast
        voiceSpeaker.speakWhenReady(response)
        Toast.makeText(this, "Reply: $response", Toast.LENGTH_LONG).show()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        voiceSpeaker.shutdown()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? = null
}
