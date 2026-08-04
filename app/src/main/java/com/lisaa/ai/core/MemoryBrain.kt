package com.lisaa.ai.core

class MemoryBrain {

    private val memory = mutableMapOf<String, String>()

    fun remember(key: String, value: String) {
        memory[key.lowercase()] = value
    }

    fun recall(key: String): String? {
        return memory[key.lowercase()]
    }

    fun forget(key: String) {
        memory.remove(key.lowercase())
    }

    fun clear() {
        memory.clear()
    }

    fun hasMemory(key: String): Boolean {
        return memory.containsKey(key.lowercase())
    }
}
