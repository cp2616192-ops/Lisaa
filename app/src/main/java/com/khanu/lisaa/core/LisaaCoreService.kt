package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.personality.PersonalityEngine
import com.khanu.lisaa.voice.VoiceSpeaker

class LisaaCoreService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var voiceSpeaker: VoiceSpeaker
    private lateinit var personalityEngine: PersonalityEngine

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

        // Set personality to GF mode for testing
        personalityEngine.setPersonality(PersonalityEngine.PersonalityType.GF)

        // Start foreground service
        val notification = notificationHelper.createServiceNotification(
            title = "LISAA AI",
            content = "Personality Engine Active",
            showActions = false
        )
        startForeground(NOTIFICATION_ID, notification)

        // Speak welcome message
        voiceSpeaker.speakWhenReady("Hello, I am LISAA. I am ready to talk.")

        sendBroadcast(Intent("LISAA_STATE_CHANGE").putExtra("state", "LISTENING"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle test commands via intent (for debugging without mic)
        intent?.getStringExtra("TEST_COMMAND")?.let { command ->
            handleTestCommand(command)
        }
        return START_STICKY
    }

    private fun handleTestCommand(command: String) {
        val response = personalityEngine.getResponse(command)
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
