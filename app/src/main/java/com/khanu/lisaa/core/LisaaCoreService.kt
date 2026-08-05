package com.khanu.lisaa.core

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.khanu.lisaa.memory.MemoryManager
import com.khanu.lisaa.notification.NotificationHelper
import com.khanu.lisaa.personality.PersonalityEngine
import com.khanu.lisaa.tools.ToolCallingSystem
import com.khanu.lisaa.voice.VoiceRecognizer
import com.khanu.lisaa.voice.VoiceSpeaker
import com.khanu.lisaa.wakeword.WakeWordEngine
import kotlinx.coroutines.*

class LisaaCoreService : Service() {

    private lateinit var stateMachine: StateMachine
    private lateinit var sessionManager: SessionManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var memoryManager: MemoryManager
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var wakeWordEngine: WakeWordEngine
    private lateinit var voiceRecognizer: VoiceRecognizer
    private lateinit var voiceSpeaker: VoiceSpeaker
    private lateinit var toolCallingSystem: ToolCallingSystem

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var isAwaitingCommand = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "ACTION_STOP" // Using "ACTION_START" later
        const val ACTION_STATE_CHANGE = "LISAA_STATE_CHANGE"
        const val EXTRA_STATE = "state"
    }

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        startForeground(NOTIFICATION_ID, notificationHelper.createServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_START" -> startServiceInternal()
            "ACTION_STOP" -> stopServiceInternal()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { stopServiceInternal(); serviceScope.cancel() }
    override fun onTaskRemoved(rootIntent: Intent?) { if (isRunning) startService(Intent(this, LisaaCoreService::class.java)) }

    private fun initializeComponents() {
        stateMachine = StateMachine()
        sessionManager = SessionManager()
        notificationHelper = NotificationHelper(applicationContext)
        memoryManager = MemoryManager()
        personalityEngine = PersonalityEngine()
        toolCallingSystem = ToolCallingSystem(applicationContext)

        wakeWordEngine = WakeWordEngine(applicationContext) { handleWakeWordDetected() }
        voiceRecognizer = VoiceRecognizer(applicationContext) { transcript -> handleUserSpeech(transcript) }
        voiceSpeaker = VoiceSpeaker(applicationContext)

        // Start with IDLE
        stateMachine.setState(AssistantState.IDLE)
    }

    private fun startServiceInternal() {
        if (isRunning) return
        isRunning = true
        sessionManager.newSession()
        stateMachine.setState(AssistantState.LISTENING)
        wakeWordEngine.startListening()
        updateNotificationAndBroadcast("Listening for 'LISAA'...")
    }

    private fun stopServiceInternal() {
        if (!isRunning) return
        isRunning = false
        wakeWordEngine.stopListening()
        voiceRecognizer.stopListening()
        voiceSpeaker.stop()
        stateMachine.setState(AssistantState.IDLE)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        updateNotificationAndBroadcast("Stopped")
    }

    private fun handleWakeWordDetected() {
        if (!isRunning || isAwaitingCommand) return
        serviceScope.launch {
            wakeWordEngine.stopListening()
            stateMachine.setState(AssistantState.THINKING)
            updateNotificationAndBroadcast("Wake word detected...")
            delay(150)
            stateMachine.setState(AssistantState.LISTENING)
            updateNotificationAndBroadcast("Listening...")
            isAwaitingCommand = true
            voiceRecognizer.startListening()
            // Timeout
            launch {
                delay(8000)
                if (isAwaitingCommand) {
                    voiceRecognizer.stopListening()
                    isAwaitingCommand = false
                    stateMachine.setState(AssistantState.LISTENING)
                    updateNotificationAndBroadcast("Listening for 'LISAA'...")
                    wakeWordEngine.startListening()
                }
            }
        }
    }

    private fun handleUserSpeech(transcript: String) {
        if (!isRunning || !isAwaitingCommand) return
        serviceScope.launch {
            voiceRecognizer.stopListening()
            isAwaitingCommand = false
            stateMachine.setState(AssistantState.THINKING)
            updateNotificationAndBroadcast("Processing...")

            // Save user message
            memoryManager.remember("User: $transcript", importance = 1)

            // Process command (use CognitiveOrchestrator or inline logic)
            val response = processCommand(transcript)

            // Save assistant response
            memoryManager.remember("Assistant: $response", importance = 1)

            stateMachine.setState(AssistantState.SPEAKING)
            updateNotificationAndBroadcast("Speaking...")
            voiceSpeaker.speak(response)
            delay(500 + (response.length * 20).coerceAtMost(3000))

            stateMachine.setState(AssistantState.LISTENING)
            updateNotificationAndBroadcast("Listening for 'LISAA'...")
            wakeWordEngine.startListening()
        }
    }

    private fun processCommand(input: String): String {
        // Use tool system if applicable
        val toolResult = toolCallingSystem.executeTool(input, emptyMap())
        if (toolResult != null && toolResult != "Tool not found") {
            return toolResult
        }
        // Fallback to personality
        return personalityEngine.getResponse(input)
    }

    private fun updateNotificationAndBroadcast(text: String) {
        val notification = notificationHelper.createServiceNotification(
            title = "LISAA AI",
            content = text,
            showActions = true
        )
        notificationHelper.updateNotification(notification)
        // Broadcast state
        val intent = Intent(ACTION_STATE_CHANGE)
        intent.putExtra(EXTRA_STATE, stateMachine.getState().name)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? {
        return toolCallingSystem.executeTool(toolName, params)
    }
}
