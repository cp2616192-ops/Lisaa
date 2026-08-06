package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.speech.SpeechRecognizer
import android.widget.Toast
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.voice.VoiceRecognizer
import com.khanu.lisaa.voice.VoiceSpeaker
import kotlinx.coroutines.*

class LisaaCoreService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private var voiceSpeaker: VoiceSpeaker? = null
    private var voiceRecognizer: VoiceRecognizer? = null
    private var isRecognizerRunning = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        fun startService(context: Context) {
            context.startForegroundService(Intent(context, LisaaCoreService::class.java))
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, LisaaCoreService::class.java))
        }
        fun startListening(context: Context) {
            (context as? LisaaCoreService)?.startListening() ?: run {
                val intent = Intent(context, LisaaCoreService::class.java)
                context.startForegroundService(intent)
            }
        }
        fun stopListening(context: Context) {
            (context as? LisaaCoreService)?.stopListening() ?: run {
                context.stopService(Intent(context, LisaaCoreService::class.java))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            notificationHelper = NotificationHelper(applicationContext)
            voiceSpeaker = VoiceSpeaker(applicationContext)
            voiceSpeaker?.speakWhenReady("Test mode ready.")

            voiceRecognizer = VoiceRecognizer(
                applicationContext,
                onResult = { transcript ->
                    Toast.makeText(this, "You said: $transcript", Toast.LENGTH_LONG).show()
                    voiceSpeaker?.speakWhenReady("You said: $transcript")
                    // Auto restart recognizer so user doesn't have to tap "Start Mic" again
                    if (isRecognizerRunning) {
                        voiceRecognizer?.startListening()
                    }
                },
                onError = { errorCode ->
                    if (errorCode != SpeechRecognizer.ERROR_NO_MATCH) {
                        Toast.makeText(this, "Error: $errorCode", Toast.LENGTH_SHORT).show()
                        isRecognizerRunning = false
                    }
                }
            )
        } catch (e: Exception) {
            Toast.makeText(this, "FATAL: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val notification = notificationHelper.createServiceNotification(
                title = "LISAA AI (Test)",
                content = "Tap 'Start Mic' to listen",
                showActions = false
            )
            startForeground(NOTIFICATION_ID, notification)
            Toast.makeText(this, "Service Ready. Tap 'Start Mic'.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Foreground Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return START_STICKY
    }

    fun startListening() {
        if (!isRecognizerRunning) {
            isRecognizerRunning = true
            voiceRecognizer?.startListening()
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopListening() {
        isRecognizerRunning = false
        voiceRecognizer?.stopListening()
        Toast.makeText(this, "Stopped listening", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        voiceRecognizer?.destroy()
        voiceSpeaker?.shutdown()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
