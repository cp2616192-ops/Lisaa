package com.lisaa.ai.core

class EmotionBrain {

    enum class Mood {
        HAPPY,
        SAD,
        ANGRY,
        CARING,
        NORMAL
    }

    fun detect(text: String): Mood {

        val t = text.lowercase()

        return when {

            t.contains("sad") ||
            t.contains("hurt") ||
            t.contains("cry") ->
                Mood.SAD

            t.contains("happy") ||
            t.contains("great") ->
                Mood.HAPPY

            t.contains("love") ||
            t.contains("miss") ||
            t.contains("care") ->
                Mood.CARING

            t.contains("angry") ||
            t.contains("gussa") ->
                Mood.ANGRY

            else ->
                Mood.NORMAL
        }
    }
}
