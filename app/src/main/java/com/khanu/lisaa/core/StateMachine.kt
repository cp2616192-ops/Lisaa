package com.khanu.lisaa.core

class StateMachine {

    private var currentState = AssistantState.IDLE

    fun getState(): AssistantState {
        return currentState
    }

    fun setState(newState: AssistantState) {

        if (currentState == newState) return

        println("State Changed : $currentState -> $newState")

        currentState = newState

    }

    fun isIdle(): Boolean {
        return currentState == AssistantState.IDLE
    }

    fun isListening(): Boolean {
        return currentState == AssistantState.LISTENING
    }

    fun isThinking(): Boolean {
        return currentState == AssistantState.THINKING
    }

    fun isSpeaking(): Boolean {
        return currentState == AssistantState.SPEAKING
    }

    fun reset() {
        currentState = AssistantState.IDLE
    }
}
