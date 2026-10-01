package com.example.educloud.habit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitEngineTest {

    private val day = 20_000L

    @Test
    fun `first ever day starts a streak of one`() {
        val t = HabitEngine.applyDailyActivity(0, 0, null, day, freezeAvailable = true, freezeLastUsedDay = null)
        assertEquals(1, t.currentStreak)
        assertEquals(1, t.longestStreak)
        assertTrue(t.countedAsActive)
    }

    @Test
    fun `same-day repeat does not inflate the streak`() {
        val t = HabitEngine.applyDailyActivity(5, 7, day, day, true, null)
        assertEquals(5, t.currentStreak)
        assertFalse(t.countedAsActive)
    }

    @Test
    fun `consecutive day continues and extends longest`() {
        val t = HabitEngine.applyDailyActivity(6, 6, day, day + 1, true, null)
        assertEquals(7, t.currentStreak)
        assertEquals(7, t.longestStreak)
    }

    @Test
    fun `one missed day consumes the freeze token and keeps the streak`() {
        val t = HabitEngine.applyDailyActivity(9, 9, day, day + 2, freezeAvailable = true, freezeLastUsedDay = null)
        assertTrue(t.freezeApplied)
        assertEquals(10, t.currentStreak)
        assertFalse(t.freezeAvailable)
        assertEquals(day + 2, t.freezeLastUsedDay)
    }

    @Test
    fun `missed day without a token resets the streak but never the record`() {
        val t = HabitEngine.applyDailyActivity(9, 12, day, day + 2, freezeAvailable = false, freezeLastUsedDay = day - 3)
        assertFalse(t.freezeApplied)
        assertEquals(1, t.currentStreak)
        assertEquals(12, t.longestStreak)
        assertTrue(t.freezeAvailable) // a fresh streak gets its token back
    }

    @Test
    fun `two or more missed days always reset even with a token`() {
        val t = HabitEngine.applyDailyActivity(9, 9, day, day + 4, freezeAvailable = true, freezeLastUsedDay = null)
        assertEquals(1, t.currentStreak)
        assertFalse(t.freezeApplied)
    }

    @Test
    fun `used freeze regenerates after seven consecutive active days`() {
        val usedOn = day + 2L
        // Daily activity keeps the streak alive while the token stays consumed...
        var t = HabitEngine.applyDailyActivity(11, 11, usedOn, usedOn + 1, freezeAvailable = false, freezeLastUsedDay = usedOn)
        assertFalse(t.freezeAvailable)
        for (offset in 2L..6L) {
            t = HabitEngine.applyDailyActivity(
                t.currentStreak, 11, usedOn + offset - 1, usedOn + offset,
                freezeAvailable = false, freezeLastUsedDay = usedOn,
            )
            assertFalse("token must stay consumed on active day +$offset", t.freezeAvailable)
            assertEquals((11 + offset).toInt(), t.currentStreak)
        }

        // ...until the seventh consecutive active day after consumption, when it returns.
        t = HabitEngine.applyDailyActivity(
            t.currentStreak, 11, usedOn + 6, usedOn + 7,
            freezeAvailable = false, freezeLastUsedDay = usedOn,
        )
        assertTrue(t.freezeAvailable)
        assertEquals(18, t.currentStreak)
    }

    @Test
    fun `xp pays only for the three mastery events`() {
        assertEquals(10, HabitEngine.xpFor(HabitEngine.XpEvent.LESSON_PASSED))
        assertEquals(5, HabitEngine.xpFor(HabitEngine.XpEvent.REVIEW_STABILIZED))
        assertEquals(8, HabitEngine.xpFor(HabitEngine.XpEvent.FIRST_TRY_CORRECT_AFTER_STRUGGLE))
        assertEquals(3, HabitEngine.XpEvent.entries.size) // no stealth event types
    }

    @Test
    fun `milestones are deterministic oldest-first`() {
        val ms = HabitEngine.milestonesFor(streakDays = 7, totalXp = 55, lessonsPassed = 1).map { it.id }
        // streak_3 before streak_7; xp_50; lesson_1 — grouped by kind, ascending within kind.
        assertEquals(listOf("streak_3", "streak_7", "xp_50", "lesson_1"), ms)
    }

    @Test
    fun `no milestones below every threshold`() {
        assertTrue(HabitEngine.milestonesFor(2, 49, 0).isEmpty())
    }

    // ── newlyReached: milestone diff between two totals snapshots ──────────

    private fun totals(streak: Int, xp: Int, lessons: Int) =
        HabitEngine.Totals(streakDays = streak, totalXp = xp, lessonsPassed = lessons)

    @Test
    fun `crossing one threshold returns exactly that milestone`() {
        val new = HabitEngine.newlyReached(totals(2, 49, 0), totals(3, 49, 0))
        assertEquals(listOf("streak_3"), new.map { it.id })
    }

    @Test
    fun `several crossed thresholds arrive grouped oldest-first`() {
        val new = HabitEngine.newlyReached(totals(2, 0, 0), totals(7, 55, 1))
        assertEquals(listOf("streak_3", "streak_7", "xp_50", "lesson_1"), new.map { it.id })
    }

    @Test
    fun `a streak reset never re-awards milestones`() {
        val new = HabitEngine.newlyReached(totals(9, 60, 2), totals(1, 60, 2))
        assertTrue(new.isEmpty())
    }

    @Test
    fun `unchanged totals yield nothing new`() {
        assertTrue(HabitEngine.newlyReached(totals(4, 30, 1), totals(4, 30, 1)).isEmpty())
    }

    // ── milestoneById: chip rendering needs id → emoji/title lookup ────────

    @Test
    fun `known ids resolve and unknown ids return null`() {
        assertEquals("⚡", HabitEngine.milestoneById("streak_7")?.emoji)
        assertEquals("7-day streak", HabitEngine.milestoneById("streak_7")?.title)
        assertNull(HabitEngine.milestoneById("nonsense"))
    }

    @Test
    fun `every milestone surfaced anywhere resolves by its own id`() {
        val all = HabitEngine.milestonesFor(100, 500, 20)
        assertTrue(all.isNotEmpty())
        all.forEach { m -> assertEquals(m, HabitEngine.milestoneById(m.id)) }
    }

    @Test
    fun `daily quiz goal is three answers`() {
        assertFalse(HabitEngine.dailyQuizGoalMet(2))
        assertTrue(HabitEngine.dailyQuizGoalMet(3))
        assertTrue(HabitEngine.dailyQuizGoalMet(4))
        assertEquals(3, HabitEngine.DAILY_QUIZ_GOAL)
    }

    @Test
    fun `catalog lists every milestone including unearned`() {
        val catalog = HabitEngine.catalogMilestones()
        assertEquals(11, catalog.size)
        assertTrue(catalog.map { it.id }.containsAll(listOf("streak_3", "xp_50", "lesson_1")))
        assertTrue(HabitEngine.milestonesFor(0, 0, 0).isEmpty())
    }
}
