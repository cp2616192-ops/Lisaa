package com.lisaa.ai.core

class AssistantController {

    fun startListening() {
        VoiceManager.setState(AssistantState.LISTENING)
    }

    fun startThinking() {
        VoiceManager.setState(AssistantState.PROCESSING)
    }

    fun startSpeaking() {
        VoiceManager.setState(AssistantState.SPEAKING)
    }

    fun idle() {
        VoiceManager.setState(AssistantState.IDLE)
    }

    fun currentState(): AssistantState {
        return VoiceManager.getState()
    }
}
