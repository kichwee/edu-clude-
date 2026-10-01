package com.example.educloud.ui.screens.chat

/**
 * One-shot handoff from a missed quiz item into the tutor.
 * Navigation arguments cannot carry a full question safely; this is process-local only.
 */
object PendingTutorPrompt {
    @Volatile
    private var pending: String = ""

    fun set(text: String) {
        pending = text.trim()
    }

    fun take(): String {
        val current = pending
        pending = ""
        return current
    }
}
