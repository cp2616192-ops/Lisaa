package com.khanu.lisaa.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    const val CHANNEL_ID = "lisaa_service"
    private const val CHANNEL_NAME = "LISAA Background Service"

    fun createChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )

            channel.description = "LISAA AI Background Assistant"
            channel.setShowBadge(false)

            val manager =
                context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(context: Context): Notification {

        return Notification.Builder(
            context,
            CHANNEL_ID
        )
            .setContentTitle("🤖 LISAA AI")
            .setContentText("Listening in background...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    }
}
