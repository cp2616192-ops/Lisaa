package com.lisaa.ai.core

class MemoryManager {

    private var userName: String? = null


    fun saveName(name: String) {
        userName = name.trim()
    }


    fun getName(): String? {
        return userName
    }


    fun hasName(): Boolean {
        return userName != null
    }


    fun clearMemory() {
        userName = null
    }
}
