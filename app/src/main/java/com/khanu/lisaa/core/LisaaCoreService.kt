package com.khanu.lisaa.core

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.khanu.lisaa.brain.ConversationBrain
import com.khanu.lisaa.memory.MemoryManager
import com.khanu.lisaa.personality.PersonalityEngine
import com.khanu.lisaa.voice.VoiceRecognizer
import com.khanu.lisaa.voice.VoiceSpeaker
import com.khanu.lisaa.wakeword.WakeWordEngine

class LisaaCoreService : Service() {

    private lateinit var stateMachine: StateMachine
    private lateinit var sessionManager: SessionManager
    private lateinit var audioManager: AudioManager
    private lateinit var memoryManager: MemoryManager
    private lateinit var conversationBrain: ConversationBrain
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var wakeWordEngine: WakeWordEngine
    private lateinit var voiceRecognizer: VoiceRecognizer
    private lateinit var voiceSpeaker: VoiceSpeaker

    override fun onCreate() {
        super.onCreate()

        // Create Notification Channel
        NotificationHelper.createChannel(this)

        stateMachine = StateMachine()
        sessionManager = SessionManager()
        audioManager = AudioManager(this)
     memoryManager = MemoryManager()

conversationBrain = ConversationBrain(memoryManager)

personalityEngine = PersonalityEngine()

wakeWordEngine = WakeWordEngine()

voiceSpeaker = VoiceSpeaker(this)

voiceRecognizer = VoiceRecognizer(
    this,
    onResult = { text ->

        if (wakeWordEngine.process(text)) {

            stateMachine.setState(AssistantState.THINKING)

            val response =
                personalityEngine.decorate(
                    conversationBrain.generateResponse(text)
                )

            stateMachine.setState(AssistantState.SPEAKING)

            voiceSpeaker.speak(response)

        }

    },
    onError = {

        stateMachine.setState(AssistantState.IDLE)

    }
)

voiceSpeaker.onSpeechFinished = {

    stateMachine.setState(AssistantState.LISTENING)

    voiceRecognizer.startListening()

}   
     
        stateMachine.setState(AssistantState.IDLE)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            101,
            createNotification()
        )

        return START_STICKY
    }

    private fun createNotification(): Notification {

        return Notification.Builder(
            this,
            NotificationHelper.CHANNEL_ID
        )
            .setContentTitle("🤖 LISAA")
            .setContentText("Running in Background")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.releaseAudioFocus()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
