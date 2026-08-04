package com.lisaa.ai.core

import java.util.LinkedList
import java.util.Queue

object ConversationQueue {

    private val queue: Queue<String> = LinkedList()

    fun add(message: String) {
        queue.add(message)
    }

    fun next(): String? {
        return if (queue.isEmpty()) null else queue.poll()
    }

    fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    fun clear() {
        queue.clear()
    }
}
