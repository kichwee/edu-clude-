package com.example.educloud.ui.screens.home

import com.example.educloud.data.model.Interaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Grade3LearningPathTest {

    @Test
    fun `empty history starts on regrouping and keeps later cards locked`() {
        val items = Grade3LearningPath.from(emptyList(), lessonsPassed = 0)
        assertEquals("two_digit_subtraction_regrouping", items.first().id)
        assertEquals(PathNodeState.InProgress, items.first().state)
        assertEquals(PathKind.Quiz, items.first().kind)
        assertTrue(items.drop(1).all { it.state == PathNodeState.Locked })
        assertEquals(5, items.size)
    }

    @Test
    fun `a passed lesson marks regrouping complete and opens the next card`() {
        val items = Grade3LearningPath.from(emptyList(), lessonsPassed = 1)
        assertEquals(PathNodeState.Completed, items.first().state)
        assertEquals(PathNodeState.InProgress, items[1].state)
        assertEquals(PathKind.Tutor, items[1].kind)
    }

    @Test
    fun `a tutor source citation completes that rule card`() {
        val counting = Grade3LearningPath.from(emptyList(), lessonsPassed = 1)[1]
        val interaction = Interaction(
            studentId = 1,
            question = "hint",
            aiResponse = "ok",
            ragSources = "${counting.id}|Term 1",
            subject = "math",
        )
        val items = Grade3LearningPath.from(listOf(interaction), lessonsPassed = 1)
        assertEquals(PathNodeState.Completed, items[1].state)
        assertEquals(PathNodeState.InProgress, items[2].state)
    }

    @Test
    fun `next open title is the in-progress node`() {
        val items = Grade3LearningPath.from(emptyList(), lessonsPassed = 1)
        assertEquals(items[1].title, Grade3LearningPath.nextOpenTitle(items))
    }
}
