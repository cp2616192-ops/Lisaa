package com.khanu.lisaa.memory

data class MemoryItem(
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1
)

class MemoryManager {

    private val shortMemory = mutableListOf<MemoryItem>()
    private val longMemory = mutableListOf<MemoryItem>()

    fun remember(text: String, importance: Int = 1) {

        val item = MemoryItem(text, System.currentTimeMillis(), importance)

        shortMemory.add(item)

        if (importance >= 5) {
            longMemory.add(item)
        }

    }

    fun getShortMemory(): List<MemoryItem> {
        return shortMemory
    }

    fun getLongMemory(): List<MemoryItem> {
        return longMemory
    }

    fun clearShortMemory() {
        shortMemory.clear()
    }

}
