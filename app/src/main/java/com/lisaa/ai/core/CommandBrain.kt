package com.lisaa.ai.core

class CommandBrain {

    fun process(input: String): String? {

        val text = input.lowercase().trim()

        return when {

            text.contains("time") ||
            text.contains("samay") ->
                "__SHOW_TIME__"

            text.contains("battery") ->
                "__SHOW_BATTERY__"

            text.contains("camera") ->
                "__OPEN_CAMERA__"

            text.contains("torch") ||
            text.contains("flash") ->
                "__TOGGLE_FLASH__"

            text.contains("wifi") ->
                "__WIFI__"

            text.contains("bluetooth") ->
                "__BLUETOOTH__"

            else -> null
        }
    }
}
