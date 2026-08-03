package com.lisaa.ai

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mic = findViewById<Button>(R.id.btnMic)

        mic.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.hello),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
