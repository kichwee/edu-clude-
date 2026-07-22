package com.example.educloud.ui.screens.featurephone

import com.example.educloud.content.Grade3MathContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeaturePhoneDemoLogicTest {
    @Test
    fun simulatorUsesTheSharedGradeThreeRootAndCountingTwosPath() {
        assertTrue(FeaturePhoneDemoLogic.ussdReply("").contains("1. Grade 3 Maths tutor"))
        assertTrue(FeaturePhoneDemoLogic.ussdReply("1").contains("Counting in twos"))
        assertTrue(FeaturePhoneDemoLogic.ussdReply("1*2").contains("Book p.14"))
        assertTrue(FeaturePhoneDemoLogic.ussdReply("1*2*2").contains("610, 612, 614"))
        assertTrue(FeaturePhoneDemoLogic.ussdReply("1*2*2*1").startsWith("END Nice work!"))
    }

    @Test
    fun simulatorCardsMatchTheAndroidTutorLessonIdsAndVersion() {
        assertEquals(
            Grade3MathContent.ruleCards.map { it.lesson.id },
            Grade3MathContent.ruleCards.map { it.lesson.id },
        )
        assertTrue(Grade3MathContent.ruleCards.all { it.lesson.version.isNotBlank() })
    }

    @Test
    fun removedGenericDemoPathIsRejected() {
        assertEquals("END That option is unavailable. Dial again.", FeaturePhoneDemoLogic.ussdReply("4"))
    }
}
