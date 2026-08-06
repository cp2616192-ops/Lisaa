package com.khanu.lisaa

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.khanu.lisaa.core.AssistantState
import com.khanu.lisaa.core.LisaaCoreService
import com.khanu.lisaa.R

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startService()
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LisaaCoreService.ACTION_STATE_CHANGE) {
                val stateName = intent.getStringExtra(LisaaCoreService.EXTRA_STATE)
                updateUI(stateName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)

        setupUI()
        checkPermissions()
        registerReceiver()
    }

    private fun setupUI() {
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
        updateUI("IDLE")
    }

    private fun updateUI(stateName: String?) {
        val state = try { AssistantState.valueOf(stateName ?: "IDLE") } catch (e: Exception) { AssistantState.IDLE }
        val status = when (state) {
            AssistantState.IDLE -> "Inactive"
            AssistantState.WAKE_DETECTED -> "Wake detected..."
            AssistantState.ACTIVE -> "Active"
            AssistantState.LISTENING -> "Listening..."
            AssistantState.THINKING -> "Thinking..."
            AssistantState.SPEAKING -> "Speaking..."
            AssistantState.BARGE_IN -> "Interrupting..."
            AssistantState.COOLDOWN -> "Cooldown"
            AssistantState.RECOVERING -> "Recovering"
            AssistantState.ERROR -> "Error"
        }
        statusText.text = "Status: $status"
        toggleButton.text = if (state == AssistantState.IDLE) "Start Service" else "Stop Service"
    }

    private fun checkPermissions() {
        if (!hasRequiredPermissions()) {
            requestPermissions()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startService() {
        if (hasRequiredPermissions()) {
            val intent = Intent(this, LisaaCoreService::class.java).apply { action = "ACTION_START" }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    private fun stopService() {
        val intent = Intent(this, LisaaCoreService::class.java).apply { action = "ACTION_STOP" }
        stopService(intent)
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            stateReceiver,
            IntentFilter(LisaaCoreService.ACTION_STATE_CHANGE)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver)
    }
}
