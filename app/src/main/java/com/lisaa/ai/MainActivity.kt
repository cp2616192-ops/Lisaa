 package com.lisaa.ai

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            val errorView = TextView(this)
            errorView.text = "LISAA CRASH:\n\n${e.message}\n\n${e.stackTraceToString()}"
            errorView.textSize = 14f
            setContentView(errorView)
        }
    }
}
