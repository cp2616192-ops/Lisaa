package com.lisaa.ai.core

class AudioFocusManager {

    var micEnabled = false
    var speakerEnabled = false

    fun enableMic() {
        micEnabled = true
        speakerEnabled = false
    }

    fun enableSpeaker() {
        speakerEnabled = true
        micEnabled = false
    }
}
