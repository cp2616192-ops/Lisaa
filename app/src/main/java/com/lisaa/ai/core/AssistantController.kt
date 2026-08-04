package com.lisaa.ai.core

import android.app.Activity

class AssistantController(private val activity: Activity) {

    private val commandBrain = CommandBrain()
    private val lisaaBrain = LisaaBrain()
    private val commandExecutor = CommandExecutor(activity)

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

    fun process(text: String): String {

        val command = commandBrain.process(text)

        if (command != null) {
            return commandExecutor.execute(command)
        }

        return lisaaBrain.process(text)
    }
}   
                                               
        
            
        
        
        
    
