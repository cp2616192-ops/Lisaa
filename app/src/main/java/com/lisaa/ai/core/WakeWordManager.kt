package com.lisaa.ai.core

object WakeWordManager {

    private val wakeWords = listOf(
        "lisaa",
        "lisa",
        "hey lisaa",
        "hello lisaa"
    )

    fun detected(text: String): Boolean {
        return wakeWords.any {
            text.lowercase().contains(it)
        }
    }
}
