package com.lisaa.ai.core

class LisaaBrain {

    fun process(input: String): String {

        val text = input.lowercase()

        return when {

            text.contains("hello") ||
            text.contains("hi") ||
            text.contains("hey") -> {
                "Hello Sir. I am LISAA. How can I help you?"
            }

            text.contains("how are you") ||
            text.contains("kaise ho") -> {
                "I am fine Sir. I am always ready to help you."
            }

            text.contains("your name") ||
            text.contains("tumhara naam") -> {
                "My name is LISAA. Your personal AI assistant."
            }

            else -> {
                "Sorry Sir, I am still learning."
            }
        }
    }
}
