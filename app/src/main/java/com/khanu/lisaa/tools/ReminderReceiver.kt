package com.khanu.lisaa.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra("text") ?: "Reminder!"
        Toast.makeText(context, "🔔 $text", Toast.LENGTH_LONG).show()
    }
}
