package com.example.educloud.ui.screens.chat

import org.junit.Assert.assertEquals
import org.junit.Test

class PendingTutorPromptTest {

    @Test
    fun `take clears the pending prompt`() {
        PendingTutorPrompt.set("Help me understand: 45 - 29")
        assertEquals("Help me understand: 45 - 29", PendingTutorPrompt.take())
        assertEquals("", PendingTutorPrompt.take())
    }
}
