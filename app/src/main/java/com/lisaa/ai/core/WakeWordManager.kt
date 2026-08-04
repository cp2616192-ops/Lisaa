package com.lisaa.ai.core

object WakeWordManager {

    private var active = false

    fun detected(text: String): Boolean {

        val input = text.lowercase().trim()

        // First activation
        if (!active) {

       if (
           input.contains("hello lisaa") ||
           input.contains("hello lisa") ||
           input.contains("hello lissa") ||
           input.contains("hey lisaa") ||
           input.contains("hey lisa") ||
           input.contains("lisaa")
     ) {
           
                
           

                active = true
                return true
            }

            return false
        }

        // Already active
        return true
    }

    fun deactivate() {
        active = false
    }

    fun isActive(): Boolean {
        return active
    }
}
