package com.khanu.lisaa.core

import android.content.Context
import com.khanu.lisaa.brain.ConversationBrain
import com.khanu.lisaa.memory.MemoryManager
import com.khanu.lisaa.personality.PersonalityEngine
import com.khanu.lisaa.voice.VoiceSpeaker

class CognitiveOrchestrator(
    context: Context
) {

    private val memoryManager = MemoryManager()

    private val personalityEngine = PersonalityEngine()

    private val conversationBrain =
        ConversationBrain(memoryManager)

    private val speaker =
        VoiceSpeaker(context)

    fun process(text: String) {

        memoryManager.remember(text)

        val response =
            conversationBrain.generateResponse(text)

        val finalResponse =
            personalityEngine.decorate(response)

        speaker.speak(finalResponse)

    }

    fun setMood(
        mood: PersonalityEngine.Mood
    ) {

        personalityEngine.setMood(mood)

    }

    fun shutdown() {

        speaker.shutdown()

    }

}
