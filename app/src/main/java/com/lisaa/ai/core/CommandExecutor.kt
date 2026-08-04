package com.lisaa.ai.core

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore

class CommandExecutor(private val activity: Activity) {

    fun execute(command: String): String {

        return when (command) {

            "__SHOW_TIME__" -> {
                "Time feature coming soon."
            }

            "__SHOW_BATTERY__" -> {
                "Battery feature coming soon."
            }

            "__OPEN_CAMERA__" -> {

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                activity.startActivity(intent)

                "Opening Camera Sir."
            }

            "__TOGGLE_FLASH__" -> {
                "Torch feature coming soon."
            }

            "__WIFI__" -> {
                "WiFi feature coming soon."
            }

            "__BLUETOOTH__" -> {
                "Bluetooth feature coming soon."
            }

            else -> {
                "Unknown command."
            }
        }
    }
}
