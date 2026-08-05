package com.khanu.lisaa.wakeword

class WakeWordEngine {

    private var active = false

    private var lastWakeTime = 0L

    private val timeout = 10 * 60 * 1000L

    fun process(text: String): Boolean {

        val input = text.lowercase()

        val now = System.currentTimeMillis()

        if (active && now - lastWakeTime > timeout) {
            active = false
        }

        // First Activation
        if (!active && input.contains("hello lisaa")) {

            active = true
            lastWakeTime = now

            return true
        }

        // Active Mode
        if (active && input.contains("lisaa")) {

            lastWakeTime = now

            return true
        }

        return false
    }

    fun deactivate() {
        active = false
    }

    fun isActive(): Boolean {
        return active
    }

} 
