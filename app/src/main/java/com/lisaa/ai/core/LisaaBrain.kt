package com.lisaa.ai.core

class LisaaBrain {

    fun process(input: String): String {

        val text = input.lowercase().trim()

        return when {

            text.contains("hello") ||
            text.contains("hi") ||
            text.contains("hey") ||
            text.contains("namaste") -> {

                "Hello Sir. I am LISAA. How can I help you?"
            }


            text.contains("how are you") ||
            text.contains("kaise ho") ||
            text.contains("kaisi ho") -> {

                "I am fine Sir. I am always ready to help you."
            }


            text.contains("your name") ||
            text.contains("what is your name") ||
            text.contains("what's your name") ||
            text.contains("tell me your name") ||
            text.contains("who are you") ||
            text.contains("tumhara naam") ||
            text.contains("aap ka naam") -> {

                "My name is LISAA. I am your personal AI assistant."
            }


            text.contains("thank you") ||
            text.contains("thanks") -> {

                "You're welcome Sir. I am always here to help you."
            }


            else -> {

                "Sorry Sir, I am still learning. Please teach me."
            }
        }
    }
}
