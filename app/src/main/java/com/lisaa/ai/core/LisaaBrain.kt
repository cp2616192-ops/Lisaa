package com.lisaa.ai.core

class LisaaBrain {

    private val emotionBrain = EmotionBrain()


    fun process(input: String): String {

        val text = input.lowercase().trim()

        val mood = emotionBrain.detect(text)


        return when {

            text.contains("hello") ||
            text.contains("hi") ||
            text.contains("hey") ||
            text.contains("namaste") -> {

                "Hello Sir. I am LISAA. How can I help you?"
            }


            text.contains("your name") ||
            text.contains("what is your name") ||
            text.contains("what's your name") ||
            text.contains("who are you") ||
            text.contains("tumhara naam") ||
            text.contains("aap ka naam") -> {

                "My name is LISAA. I am your personal AI assistant."
            }


            text.contains("how are you") ||
            text.contains("kaise ho") ||
            text.contains("kaisi ho") -> {

                "I am fine Sir. I am always ready to help you."
            }


            mood == EmotionBrain.Mood.SAD -> {

                "I am here with you Sir. Batao kya hua, I am listening."
            }


            mood == EmotionBrain.Mood.HAPPY -> {

                "That's great Sir. I am happy to hear that."
            }


            mood == EmotionBrain.Mood.CARING -> {

                "I am always here for you Sir. You can talk to me."
            }


            mood == EmotionBrain.Mood.ANGRY -> {

                "I understand Sir. Try to stay calm, I am here to help."
            }


            else -> {

                "Sorry Sir, I am still learning."
            }
        }
    }
}
