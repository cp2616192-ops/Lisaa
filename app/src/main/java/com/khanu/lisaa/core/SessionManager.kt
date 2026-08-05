package com.khanu.lisaa.core

class SessionManager {

    var sessionId = System.currentTimeMillis()

    fun newSession() {
        sessionId = System.currentTimeMillis()
    }
}
