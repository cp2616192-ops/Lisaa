package com.khanu.lisaa.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.khanu.lisaa.brain.CognitiveOrchestrator
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
    private lateinit var cognitiveOrchestrator: CognitiveOrchestrator
    private lateinit var toolCallingSystem: ToolCallingSystem

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var isAwaitingCommand = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        fun startService(context: Context) {
            val intent = Intent(context, LisaaCoreService::class.java).apply { action = ACTION_START }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, LisaaCoreService::class.java).apply { action = ACTION_STOP })
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        startForeground(NOTIFICATION_ID, notificationHelper.createServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startServiceInternal()
            ACTION_STOP -> stopServiceInternal()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { stopServiceInternal(); serviceScope.cancel() }
    override fun onTaskRemoved(rootIntent: Intent?) { if (isRunning) startService(Intent(this, LisaaCoreService::class.java)) }

    private fun initializeComponents() {
        stateMachine = StateMachine()
        sessionManager = SessionManager(applicationContext)
        notificationHelper = NotificationHelper(applicationContext)
        memoryManager = MemoryManager(applicationContext)
        personalityEngine = PersonalityEngine()
        toolCallingSystem = ToolCallingSystem(applicationContext)

        wakeWordEngine = WakeWordEngine(applicationContext) { handleWakeWordDetected() }
        voiceRecognizer = VoiceRecognizer(applicationContext) { transcript -> handleUserSpeech(transcript) }
        voiceSpeaker = VoiceSpeaker(applicationContext)

        cognitiveOrchestrator = CognitiveOrchestrator(memoryManager, personalityEngine, applicationContext)
    }

    private fun startServiceInternal() {
        if (isRunning) return
        isRunning = true
        stateMachine.transition(AssistantState.Listening)
        wakeWordEngine.startListening()
        updateNotification("Listening for 'LISAA'...")
    }

    private fun stopServiceInternal() {
        if (!isRunning) return
        isRunning = false
        wakeWordEngine.stopListening()
        voiceRecognizer.stopListening()
        voiceSpeaker.stop()
        stateMachine.transition(AssistantState.Inactive)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleWakeWordDetected() {
        if (!isRunning || isAwaitingCommand) return
        serviceScope.launch {
            wakeWordEngine.stopListening()
            stateMachine.transition(AssistantState.Processing)
            updateNotification("Wake word detected...")
            delay(150)
            stateMachine.transition(AssistantState.Listening)
            updateNotification("Listening...")
            isAwaitingCommand = true
            voiceRecognizer.startListening()
            launch {
                delay(8000)
                if (isAwaitingCommand) {
                    voiceRecognizer.stopListening()
                    isAwaitingCommand = false
                    stateMachine.transition(AssistantState.Listening)
                    updateNotification("Listening for 'LISAA'...")
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
            stateMachine.transition(AssistantState.Processing)
            updateNotification("Processing...")
            sessionManager.addUserMessage(transcript)
            memoryManager.remember(transcript)

            val response = cognitiveOrchestrator.processInput(transcript)
            sessionManager.addAssistantMessage(response)
            memoryManager.remember(response)

            stateMachine.transition(AssistantState.Speaking)
            updateNotification("Speaking...")
            voiceSpeaker.speak(response)
            delay(500 + (response.length * 20).coerceAtMost(3000))

            stateMachine.transition(AssistantState.Listening)
            updateNotification("Listening for 'LISAA'...")
            wakeWordEngine.startListening()
        }
    }

    private fun updateNotification(text: String) {
        val notification = notificationHelper.createServiceNotification(
            title = "LISAA AI",
            content = text,
            showActions = true
        )
        notificationHelper.updateNotification(notification)
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? {
        return toolCallingSystem.executeTool(toolName, params)
    }
}
