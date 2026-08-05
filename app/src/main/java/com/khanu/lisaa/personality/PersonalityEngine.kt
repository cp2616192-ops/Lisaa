package com.khanu.lisaa.personality

class PersonalityEngine {

    enum class Mood {
        HAPPY, SAD, ROMANTIC, PLAYFUL, ANGRY, JEALOUS, SARCASTIC, CALM, SERIOUS
    }

    enum class PersonalityType {
        FRIENDLY, PROFESSIONAL, GF, JARVIS
    }

    private var currentMood = Mood.CALM
    private var currentPersonality = PersonalityType.FRIENDLY
    private var affectionLevel = 50

    // ---------- Mood & Personality Setters ----------
    fun setMood(mood: Mood) { currentMood = mood }
    fun getMood(): Mood = currentMood
    fun setPersonality(type: PersonalityType) { currentPersonality = type }
    fun getPersonality(): PersonalityType = currentPersonality

    // ---------- Old decorate() function (kept for compatibility) ----------
    fun decorate(text: String): String {
        return when (currentMood) {
            Mood.HAPPY -> "$text 😊"
            Mood.SAD -> "$text ❤️"
            Mood.ROMANTIC -> "$text 💕"
            Mood.PLAYFUL -> "$text 😜"
            Mood.ANGRY -> "$text 😤"
            Mood.JEALOUS -> "$text 🤨"
            Mood.SARCASTIC -> "$text 🙄"
            Mood.CALM, Mood.SERIOUS -> text
        }
    }

    // ---------- JARVIS Style Responses ----------
    fun getJarvisResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") -> "Hello, sir. I trust you're well."
            lower.contains("weather") -> "I recommend a jacket. It's quite cold in the lab."
            lower.contains("open") -> "Opening that for you, sir."
            lower.contains("thank") -> "You're welcome. I aim to please."
            lower.contains("joke") -> "Why did the AI go to the doctor? It had a byte."
            else -> "I'm listening. Shall I proceed?"
        }
    }

    // ---------- GF (Girlfriend) Style Responses ----------
    fun getGFResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("love") || lower.contains("miss") -> "Aww, I love you too baby! ❤️"
            lower.contains("angry") || lower.contains("hate") -> "Hmph! Fine, I'm not talking to you right now. 😤"
            lower.contains("sad") || lower.contains("cry") -> "Hey, what's wrong? Tell me everything. 🥺"
            lower.contains("funny") || lower.contains("😂") -> "Hehe, you're so funny! 😜"
            lower.contains("jealous") || lower.contains("who") -> "Who were you talking to? Hmm? 🤨"
            lower.contains("hello") || lower.contains("hi") -> "Hey baby! I was just thinking about you. 😘"
            else -> "I'm so happy to talk to you! 😊"
        }
    }

    // ---------- Automatic Response Router ----------
    fun getResponse(input: String): String {
        return when (currentPersonality) {
            PersonalityType.JARVIS -> getJarvisResponse(input)
            PersonalityType.GF -> getGFResponse(input)
            else -> {
                // Friendly or Professional - use decorate logic
                val base = when {
                    input.contains("how are you") -> "I'm doing great, thanks for asking!"
                    input.contains("hello") -> "Hello there! Nice to see you."
                    else -> "I understand. Let me help you with that."
                }
                decorate(base)
            }
        }
    }

    // ---------- GF Affection System ----------
    fun getAffectionLevel(): Int = affectionLevel
    fun increaseAffection() { if (affectionLevel < 100) affectionLevel += 5 }
    fun decreaseAffection() { if (affectionLevel > 0) affectionLevel -= 5 }
}
