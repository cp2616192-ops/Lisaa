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
    private lateinit var toggleButton: Button

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
        toggleButton = findViewById(R.id.toggleButton)

        toggleButton.setOnClickListener {
            if (toggleButton.text == "Stop Service") {
                stopService()
            } else {
                if (hasRequiredPermissions()) {
                    startService()
                } else {
                    requestPermissions()
                }
            }
        }
        statusText.text = "Status: Inactive"
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
        statusText.text = "Status: Listening..."
        toggleButton.text = "Stop Service"
    }

    private fun stopService() {
        LisaaCoreService.stopService(this)
        statusText.text = "Status: Inactive"
        toggleButton.text = "Start Service"
    }
}
