package com.khanu.lisaa.brain

import com.khanu.lisaa.memory.MemoryManager

class ConversationBrain(
    private val memoryManager: MemoryManager
) {

    fun generateResponse(userText: String): String {

        memoryManager.remember(userText)

        return when {

            userText.contains("hello", true) ->
                "Hello Sir ❤️"

            userText.contains("hi", true) ->
                "Hi Sir 😊"

            userText.contains("time", true) ->
                "Main abhi time check kar rahi hoon."

            userText.contains("thank", true) ->
                "Always for you Sir ❤️"

            else ->
                "Main samajh rahi hoon Sir..."
        }

    }

}
