package com.khanu.lisaa.core

import android.content.Context
import android.media.AudioManager

class AudioManager(private val context: Context) {

    private val systemAudio =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun requestAudioFocus() {
        systemAudio.mode = AudioManager.MODE_IN_COMMUNICATION
        systemAudio.isSpeakerphoneOn = false
    }

    fun releaseAudioFocus() {
        systemAudio.mode = AudioManager.MODE_NORMAL
    }

    fun muteSystem() {
        // Future implementation
    }

    fun unMuteSystem() {
        // Future implementation
    }
}
