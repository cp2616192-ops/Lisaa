package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.voice.VoiceSpeaker

class LisaaCoreService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var voiceSpeaker: VoiceSpeaker

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

        // 1. VoiceSpeaker Initialize
        voiceSpeaker = VoiceSpeaker(applicationContext)
        // Wait for TTS to be ready (it will be ready after some milliseconds)
        // We'll speak after a short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            voiceSpeaker.speak("Hello, I am LISAA. Service is running.")
        }, 1000)

        // Foreground Notification
        val notification = notificationHelper.createServiceNotification(
            title = "LISAA AI (Test)",
            content = "Voice Speaker Active",
            showActions = false
        )
        startForeground(NOTIFICATION_ID, notification)

        sendBroadcast(Intent("LISAA_STATE_CHANGE").putExtra("state", "LISTENING"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        voiceSpeaker.shutdown()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? = null
}
