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
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val NOTIFICATION_ID = 1001
        fun startService(context: Context) {
            context.startForegroundService(Intent(context, LisaaCoreService::class.java))
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, LisaaCoreService::class.java))
        }
        fun startListening(context: Context) {
            (context as? LisaaCoreService)?.startListening()
        }
        fun stopListening(context: Context) {
            (context as? LisaaCoreService)?.stopListening()
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            notificationHelper = NotificationHelper(applicationContext)
            voiceSpeaker = VoiceSpeaker(applicationContext)
            // Ab start me TTS "Test mode" nahi bolega, direct service ready rahega
            voiceSpeaker?.speakWhenReady("Service ready. Tap Start Mic.")

            voiceRecognizer = VoiceRecognizer(
                applicationContext,
                onResult = { transcript ->
                    Toast.makeText(this, "You said: $transcript", Toast.LENGTH_LONG).show()
                    voiceSpeaker?.speakWhenReady("You said: $transcript")
                    // Recognizer automatically restart hoga, but let TTS finish first
                    serviceScope.launch {
                        delay(2000) // TTS ko bolne do
                        if (isRecognizerRunning) {
                            voiceRecognizer?.startListening()
                            Toast.makeText(this@LisaaCoreService, "Listening...", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onError = { errorCode ->
                    if (errorCode != SpeechRecognizer.ERROR_NO_MATCH) {
                        Toast.makeText(this, "Error Code: $errorCode", Toast.LENGTH_SHORT).show()
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
                content = "Tap 'Start Mic'",
                showActions = false
            )
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Toast.makeText(this, "Foreground Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return START_STICKY
    }

    fun startListening() {
        if (!isRecognizerRunning) {
            // ✅ FIX: Pehle TTS ko Roko (agar kuch bol raha hai)
            voiceSpeaker?.stop()
            
            // ✅ FIX: 1.5 second wait karo microphone release hone ke liye
            serviceScope.launch {
                Toast.makeText(this@LisaaCoreService, "Preparing mic...", Toast.LENGTH_SHORT).show()
                delay(1500)
                
                isRecognizerRunning = true
                voiceRecognizer?.startListening()
                Toast.makeText(this@LisaaCoreService, "Listening... Speak Now!", Toast.LENGTH_SHORT).show()
            }
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
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
