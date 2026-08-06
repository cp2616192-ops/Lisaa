package com.khanu.lisaa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.khanu.lisaa.core.LisaaCoreService

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var startMicButton: Button
    private lateinit var stopMicButton: Button
    private lateinit var toggleServiceButton: Button

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        startMicButton = findViewById(R.id.startMicButton)
        stopMicButton = findViewById(R.id.stopMicButton)
        toggleServiceButton = findViewById(R.id.toggleButton)

        toggleServiceButton.setOnClickListener {
            if (toggleServiceButton.text == "Stop Service") {
                stopService()
            } else {
                if (hasRequiredPermissions()) {
                    startService()
                } else {
                    requestPermissions()
                }
            }
        }

        startMicButton.setOnClickListener {
            LisaaCoreService.startListening(this)
            statusText.text = "Status: Listening..."
        }

        stopMicButton.setOnClickListener {
            LisaaCoreService.stopListening(this)
            statusText.text = "Status: Inactive"
        }

        statusText.text = "Status: Inactive"
        startMicButton.isEnabled = false
        stopMicButton.isEnabled = false
    }

    private fun hasRequiredPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.POST_NOTIFICATIONS
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startService() {
        LisaaCoreService.startService(this)
        statusText.text = "Status: Service Active"
        toggleServiceButton.text = "Stop Service"
        startMicButton.isEnabled = true
        stopMicButton.isEnabled = true
    }

    private fun stopService() {
        LisaaCoreService.stopService(this)
        statusText.text = "Status: Inactive"
        toggleServiceButton.text = "Start Service"
        startMicButton.isEnabled = false
        stopMicButton.isEnabled = false
    }
}
