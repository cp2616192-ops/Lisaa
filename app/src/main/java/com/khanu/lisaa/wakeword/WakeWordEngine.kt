package com.khanu.lisaa.wakeword

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

class WakeWordEngine(
    private val context: Context,
    private val onWakeWordDetected: () -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRunning = AtomicBoolean(false)
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val readBufferSize = bufferSize * 2
    private val speechThreshold = 0.018f
    private val silenceThreshold = 0.006f
    private val minSpeechDurationMs = 400L
    private val maxSilenceDurationMs = 700L
    private var isSpeechDetected = false
    private var speechBuffer = mutableListOf<ByteArray>()
    private var silenceStartTime = 0L
    private var speechStartTime = 0L

    fun startListening(): Boolean {
        if (_isListening.value) return false
        if (isRunning.get()) return false
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 4
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord = null
                return false
            }
            audioRecord?.startRecording()
            _isListening.value = true
            isRunning.set(true)
            resetState()
            engineScope.launch { processAudioStream() }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun stopListening() {
        isRunning.set(false)
        _isListening.value = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        resetState()
    }

    private fun resetState() {
        isSpeechDetected = false
        speechBuffer.clear()
        silenceStartTime = 0L
        speechStartTime = 0L
    }

    private suspend fun processAudioStream() {
        val buffer = ByteArray(readBufferSize)
        while (isRunning.get() && _isListening.value) {
            val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (bytesRead <= 0) { delay(10); continue }
            val data = buffer.copyOf(bytesRead)
            val energy = calculateEnergy(data)
            if (energy > speechThreshold && !isSpeechDetected) {
                isSpeechDetected = true
                speechStartTime = System.currentTimeMillis()
                speechBuffer.clear()
                speechBuffer.add(data)
                silenceStartTime = 0L
            } else if (isSpeechDetected) {
                speechBuffer.add(data)
                if (energy < silenceThreshold) {
                    if (silenceStartTime == 0L) silenceStartTime = System.currentTimeMillis()
                    else {
                        val silenceDuration = System.currentTimeMillis() - silenceStartTime
                        if (silenceDuration > maxSilenceDurationMs) {
                            // Simulate detection (replace with ML later)
                            // For now, just trigger on any speech (you can replace with actual keyword detection)
                            // But we want it to trigger on "LISAA".
                            // For testing, we trigger on ANY speech and let the user test.
                            engineScope.launch {
                                withContext(Dispatchers.Main) {
                                    onWakeWordDetected.invoke()
                                }
                            }
                            resetState()
                        }
                    }
                } else {
                    silenceStartTime = 0L
                }
                if (speechBuffer.size > 500) {
                    engineScope.launch {
                        withContext(Dispatchers.Main) {
                            onWakeWordDetected.invoke()
                        }
                    }
                    resetState()
                }
            }
            if (isSpeechDetected && System.currentTimeMillis() - speechStartTime > 5000) {
                engineScope.launch {
                    withContext(Dispatchers.Main) {
                        onWakeWordDetected.invoke()
                    }
                }
                resetState()
            }
            delay(15)
        }
    }

    private fun calculateEnergy(buffer: ByteArray): Float {
        var sum = 0L
        for (i in 0 until buffer.size - 1 step 2) {
            val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
            sum += (sample * sample).toLong()
        }
        val rms = sqrt((sum / (buffer.size / 2)).toDouble())
        return (rms / 32768.0).toFloat()
    }

    fun destroy() { stopListening(); engineScope.cancel() }
}
