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
import androidx.lifecycle.lifecycleScope
import com.khanu.lisaa.core.AssistantState
import com.khanu.lisaa.core.LisaaCoreService
import com.khanu.lisaa.core.StateMachine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var stateMachine: StateMachine
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stateMachine = StateMachine()

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        toggleButton.setOnClickListener {
            if (stateMachine.isActive()) {
                stopService()
            } else {
                if (hasRequiredPermissions()) {
                    startService()
                } else {
                    requestPermissions()
                }
            }
        }

        lifecycleScope.launch {
            stateMachine.currentState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: AssistantState) {
        statusText.text = when (state) {
            AssistantState.Inactive -> "Inactive"
            AssistantState.Listening -> "Listening for 'LISAA'..."
            AssistantState.Processing -> "Processing..."
            AssistantState.Speaking -> "Speaking..."
            AssistantState.Error -> "Error"
        }

        toggleButton.text = if (state == AssistantState.Inactive) {
            "Start Service"
        } else {
            "Stop Service"
        }
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
            LisaaCoreService.startService(this)
        }
    }

    private fun stopService() {
        LisaaCoreService.stopService(this)
    }
}
