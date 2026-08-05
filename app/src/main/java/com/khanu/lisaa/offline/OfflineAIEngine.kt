package com.khanu.lisaa.offline

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineAIEngine(private val context: Context) {

    data class IntentResult(val intent: String, val confidence: Float, val entities: Map<String, String>)

    suspend fun analyzeIntent(text: String): IntentResult = withContext(Dispatchers.IO) {
        val lower = text.lowercase()
        val entities = mutableMapOf<String, String>()

        val intent = when {
            lower.contains("open") || lower.contains("launch") -> "open_app"
            lower.contains("search") || lower.contains("find") -> "search"
            lower.contains("call") || lower.contains("dial") -> "call"
            lower.contains("message") || lower.contains("text") || lower.contains("send") -> "message"
            lower.contains("play") || lower.contains("music") -> "play"
            lower.contains("stop") -> "stop"
            lower.contains("volume") -> "volume"
            lower.contains("wifi") -> "wifi"
            lower.contains("bluetooth") -> "bluetooth"
            lower.contains("alarm") -> "alarm"
            lower.contains("reminder") -> "reminder"
            lower.contains("weather") -> "weather"
            lower.contains("news") -> "news"
            lower.contains("photo") || lower.contains("camera") -> "camera"
            lower.contains("battery") -> "battery"
            lower.contains("screenshot") -> "screenshot"
            else -> "general"
        }

        // Extract app name / contact
        val patterns = listOf("open ", "call ", "message ")
        for (p in patterns) {
            if (lower.contains(p)) {
                val start = lower.indexOf(p) + p.length
                val end = lower.indexOf(" ", start)
                val name = if (end > start) lower.substring(start, end) else lower.substring(start)
                entities["target"] = name
            }
        }
        IntentResult(intent, 0.7f, entities)
    }

    suspend fun generateResponse(intent: String, entities: Map<String, String>): String = withContext(Dispatchers.IO) {
        when (intent) {
            "open_app" -> "Opening ${entities["target"] ?: "app"}"
            "call" -> "Calling ${entities["target"] ?: "contact"}"
            "message" -> "Messaging ${entities["target"] ?: "contact"}"
            "search" -> "Searching for ${entities["target"] ?: "it"}"
            "weather" -> "Fetching weather"
            "news" -> "Fetching news"
            "volume" -> "Setting volume"
            "wifi" -> "Toggling WiFi"
            "battery" -> "Battery: ${getBatteryLevel()}"
            else -> "I'm here. How can I help you offline?"
        }
    }

    private fun getBatteryLevel(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY).toString() + "%"
        } else "unknown"
    }
}
