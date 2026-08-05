package com.khanu.lisaa.proactive

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import java.util.*

class ProactiveEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        scope.launch {
            while (isRunning) {
                checkTriggers()
                delay(60000) // Check every 1 minute
            }
        }
    }

    fun destroy() {
        isRunning = false
        scope.cancel()
    }

    private fun checkTriggers() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Good Morning (6 AM - 10 AM) – once per day
        if (hour in 6..10 && !wasTriggeredToday("good_morning")) {
            notifyUser("Good morning! Hope you have a great day ahead! ☀️")
            markTriggered("good_morning")
        }

        // Good Night (10 PM - 12 AM)
        if (hour in 22..23 && !wasTriggeredToday("good_night")) {
            notifyUser("Good night! Sleep well and sweet dreams! 🌙")
            markTriggered("good_night")
        }

        // Random check-in (only if last check-in > 3 hours ago)
        val lastCheckin = getLastTriggerTime("checkin")
        if (System.currentTimeMillis() - lastCheckin > 3 * 60 * 60 * 1000) {
            notifyUser("Hey! Just checking in. How are you doing? 😊")
            markTriggered("checkin")
        }
    }

    private fun notifyUser(message: String) {
        handler.post {
            // We'll send to notification or voice – will be integrated in CoreService
            // For now, just send a broadcast or toast – but we'll let CoreService handle it.
            // Sending a broadcast so LisaaCoreService can pick it up.
            val intent = android.content.Intent("LISAA_PROACTIVE_ALERT")
            intent.putExtra("message", message)
            context.sendBroadcast(intent)
        }
    }

    private fun wasTriggeredToday(id: String): Boolean {
        val prefs = context.getSharedPreferences("proactive", Context.MODE_PRIVATE)
        val last = prefs.getLong("trigger_$id", 0)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastDay = Calendar.getInstance().apply { timeInMillis = last }.get(Calendar.DAY_OF_YEAR)
        return today == lastDay
    }

    private fun markTriggered(id: String) {
        val prefs = context.getSharedPreferences("proactive", Context.MODE_PRIVATE)
        prefs.edit().putLong("trigger_$id", System.currentTimeMillis()).apply()
    }

    private fun getLastTriggerTime(id: String): Long {
        val prefs = context.getSharedPreferences("proactive", Context.MODE_PRIVATE)
        return prefs.getLong("trigger_$id", 0)
    }
}
