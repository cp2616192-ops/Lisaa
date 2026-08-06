package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.khanu.lisaa.notification.NotificationHelper

class LisaaCoreService : Service() {

    private lateinit var notificationHelper: NotificationHelper

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

        // Start Foreground Service with simple notification
        val notification = notificationHelper.createServiceNotification(
            title = "LISAA AI (Test)",
            content = "Service is running!",
            showActions = false
        )
        startForeground(NOTIFICATION_ID, notification)

        // Broadcast to update UI
        sendBroadcast(Intent("LISAA_STATE_CHANGE").putExtra("state", "LISTENING"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? { return null }
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
