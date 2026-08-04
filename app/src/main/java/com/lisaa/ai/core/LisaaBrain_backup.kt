package com.lisaa.ai.core

class LisaaBrain {

    private val memoryBrain = MemoryBrain()
    private val emotionBrain = EmotionBrain()

    fun process(input: String): String {

        val text = input.lowercase().trim()

        // ===== MEMORY =====

        if (text.startsWith("my name is ")) {

            val name = input.substringAfter("my name is ").trim()

            memoryBrain.remember("username", name)

            return "Okay Sir. I will remember that your name is $name."
        }

        if (text.contains("what is my name")) {

            val name = memoryBrain.recall("username")

            return if (name != null) {
                "Sir, your name is $name."
            } else {
                "Sorry Sir, you haven't told me your name yet."
            }
        }

        // ===== EMOTION =====

        return when (emotionBrain.detect(text)) {

            EmotionBrain.Mood.SAD ->
                "Don't worry Sir. I am always with you. 💙"

            EmotionBrain.Mood.HAPPY ->
                "That's wonderful Sir! 😊"

            EmotionBrain.Mood.CARING ->
                "I care about you Sir. 💙"

            EmotionBrain.Mood.ANGRY ->
                "Please stay calm Sir. Everything will be okay."

            EmotionBrain.Mood.NORMAL -> {

                when {

                    text.contains("hello") ||
                    text.contains("hi") ||
                    text.contains("hey") ||
                    text.contains("namaste") ->
                        "Hello Sir. I am LISAA. How can I help you?"

                    text.contains("how are you") ||
                    text.contains("kaise ho") ||
                    text.contains("kaisi ho") ->
                        "I am fine Sir. I am always ready to help you."

                    text.contains("your name") ||
                    text.contains("who are you") ->
                        "My name is LISAA. I am your personal AI assistant."

                    text.contains("thank you") ||
                    text.contains("thanks") ->
                        "You're welcome Sir. I am always here to help you."

                    else ->
                        "Sorry Sir, I am still learning. Please teach me."
                }
            }
        }
    }
} 
