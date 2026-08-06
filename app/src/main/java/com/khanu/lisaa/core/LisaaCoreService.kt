package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.voice.VoiceSpeaker

class LisaaCoreService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var voiceSpeaker: VoiceSpeaker
    private val mainHandler = Handler(Looper.getMainLooper())

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

        // Start foreground service
        val notification = notificationHelper.createServiceNotification(
            title = "LISAA AI (Test)",
            content = "Voice Speaker Active",
            showActions = false
        )
        startForeground(NOTIFICATION_ID, notification)

        // Try to speak with retry
        tryToSpeak()

        sendBroadcast(Intent("LISAA_STATE_CHANGE").putExtra("state", "LISTENING"))
    }

    private fun tryToSpeak(attempt: Int = 0) {
        if (attempt > 3) {
            mainHandler.post {
                Toast.makeText(this, "TTS not ready after 3 attempts!", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val success = voiceSpeaker.speak("Hello, I am LISAA. Service is running.")
        if (success) {
            Toast.makeText(this, "TTS Speaking!", Toast.LENGTH_SHORT).show()
        } else {
            // Retry after 1 second
            mainHandler.postDelayed({
                tryToSpeak(attempt + 1)
            }, 1000)
        }
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
