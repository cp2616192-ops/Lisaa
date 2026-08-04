package com.lisaa.ai.core

class LisaaBrain {

    private val emotionBrain = EmotionBrain()
    private val memoryManager = MemoryManager()


    fun process(input: String): String {

        val text = input.lowercase().trim()

        val mood = emotionBrain.detect(text)


        if (text.startsWith("my name is")) {

            val name = text
                .replace("my name is", "")
                .trim()

            memoryManager.saveName(name)

            return "Nice to meet you $name. I will remember your name."
        }


        if (text.contains("what is my name") ||
            text.contains("mera naam kya hai")) {

            val name = memoryManager.getName()

            return if (name != null) {
                "Your name is $name."
            } else {
                "I don't know your name yet."
            }
        }


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
