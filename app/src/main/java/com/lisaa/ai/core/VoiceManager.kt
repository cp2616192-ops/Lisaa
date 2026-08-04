package com.lisaa.ai.core

object VoiceManager {

    var currentState = AssistantState.IDLE

    fun setState(state: AssistantState) {
        currentState = state
    }

    fun getState(): AssistantState {
        return currentState
    }
}
