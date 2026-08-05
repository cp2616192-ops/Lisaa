package com.khanu.lisaa.emotion

import android.content.Context

class EmotionEngine(private val context: Context) {

    enum class Mood {
        HAPPY, SAD, ROMANTIC, PLAYFUL, ANGRY, JEALOUS
    }

    private var currentMood = Mood.HAPPY
    private var affectionLevel = 50

    fun analyzeSentiment(text: String): Mood {
        val lower = text.lowercase()
        return when {
            lower.contains("love") || lower.contains("miss") || lower.contains("❤️") -> Mood.ROMANTIC
            lower.contains("angry") || lower.contains("hate") || lower.contains("😡") -> Mood.ANGRY
            lower.contains("sad") || lower.contains("cry") || lower.contains("😢") -> Mood.SAD
            lower.contains("funny") || lower.contains("😂") -> Mood.PLAYFUL
            lower.contains("jealous") || lower.contains("🤨") -> Mood.JEALOUS
            else -> Mood.HAPPY
        }
    }

    fun getGFResponse(input: String): String {
        val mood = analyzeSentiment(input)
        return when (mood) {
            Mood.ROMANTIC -> "Aww, I love you too baby! ❤️"
            Mood.ANGRY -> "Hmph! Fine, I'm not talking to you right now. 😤"
            Mood.SAD -> "Hey, what's wrong? Tell me everything, I'm here for you. 🥺"
            Mood.PLAYFUL -> "Hehe, you're so funny! 😜"
            Mood.JEALOUS -> "Who were you talking to? Hmm? 🤨"
            else -> "I'm so happy to talk to you! 😊"
        }
    }

    fun getAffectionLevel(): Int = affectionLevel
    fun increaseAffection() { if (affectionLevel < 100) affectionLevel += 5 }
    fun decreaseAffection() { if (affectionLevel > 0) affectionLevel -= 5 }
}
