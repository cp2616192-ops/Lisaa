package com.khanu.lisaa.brain

import android.content.Context
import com.khanu.lisaa.core.LisaaCoreService
import com.khanu.lisaa.memory.MemoryManager
import com.khanu.lisaa.personality.PersonalityEngine

class CognitiveOrchestrator(
    private val memoryManager: MemoryManager,
    private val personalityEngine: PersonalityEngine,
    private val context: Context
) {
    suspend fun processInput(input: String): String {
        val service = getService()
        val toolResult = service?.executeTool(input, emptyMap())
        if (toolResult != null && toolResult != "Tool not found") {
            return toolResult
        }
        return personalityEngine.getResponse(input)
    }

    private fun getService(): LisaaCoreService? {
        return try {
            (context as? LisaaCoreService) ?: (context.applicationContext as? LisaaCoreService)
        } catch (e: Exception) {
            null
        }
    }
}
