package com.example.educloud.habit

/**
 * Pure habit-loop rules (V2 plan D10-D15). No Android imports so the whole
 * engine stays JVM-unit-testable.
 *
 * D10 connectivity grace: a single missed day consumes an automatic freeze
 * token instead of resetting the streak; tokens regenerate weekly. Duolingo's
 * 850-day-loss failure mode is "a normal Kenyan Tuesday", so grace is the
 * default, not a paid perk.
 *
 * D11 mastery-only XP: raw activity never pays. Only the three named mastery
 * events are worth anything.
 */
object HabitEngine {

    /** One automatic freeze token regenerates every this-many active-day checks. */
    const val FREEZE_REGENERATION_DAYS = 7L

    const val XP_LESSON_PASSED = 10
    const val XP_REVIEW_STABILIZED = 5
    const val XP_FIRST_TRY_CORRECT_AFTER_STRUGGLE = 8

    enum class XpEvent { LESSON_PASSED, REVIEW_STABILIZED, FIRST_TRY_CORRECT_AFTER_STRUGGLE }

    fun xpFor(event: XpEvent): Int = when (event) {
        XpEvent.LESSON_PASSED -> XP_LESSON_PASSED
        XpEvent.REVIEW_STABILIZED -> XP_REVIEW_STABILIZED
        XpEvent.FIRST_TRY_CORRECT_AFTER_STRUGGLE -> XP_FIRST_TRY_CORRECT_AFTER_STRUGGLE
    }

    data class StreakTransition(
        val currentStreak: Int,
        val longestStreak: Int,
        val freezeAvailable: Boolean,
        val freezeLastUsedDay: Long?,
        /** True when today continued the streak (with or without consuming a freeze). */
        val countedAsActive: Boolean,
        /** True when the freeze token absorbed the missed day. */
        val freezeApplied: Boolean,
    )

    /**
     * @param lastActiveEpochDay epoch-day of previous activity, or null for a first-ever day.
     */
    fun applyDailyActivity(
        currentStreak: Int,
        longestStreak: Int,
        lastActiveEpochDay: Long?,
        todayEpochDay: Long,
        freezeAvailable: Boolean,
        freezeLastUsedDay: Long?,
    ): StreakTransition {
        if (lastActiveEpochDay == null || todayEpochDay <= lastActiveEpochDay) {
            // First-ever day or already counted today.
            return StreakTransition(
                currentStreak = if (lastActiveEpochDay == null) 1 else currentStreak,
                longestStreak = if (lastActiveEpochDay == null) 1 else longestStreak,
                freezeAvailable = freezeAvailable,
                freezeLastUsedDay = freezeLastUsedDay,
                countedAsActive = lastActiveEpochDay == null,
                freezeApplied = false,
            )
        }

        return when (todayEpochDay - lastActiveEpochDay) {
            1L -> continueStreak(currentStreak, longestStreak, todayEpochDay, freezeAvailable, freezeLastUsedDay, freezeApplied = false)
            2L -> if (freezeAvailable) {
                continueStreak(currentStreak, longestStreak, todayEpochDay, freezeAvailable = false, freezeLastUsedDay = todayEpochDay, freezeApplied = true)
            } else {
                reset(longestStreak)
            }
            else -> reset(longestStreak)
        }
    }

    private fun continueStreak(
        currentStreak: Int,
        longestStreak: Int,
        todayEpochDay: Long,
        freezeAvailable: Boolean,
        freezeLastUsedDay: Long?,
        freezeApplied: Boolean,
    ): StreakTransition {
        val newStreak = currentStreak + 1
        // A used token regenerates after a full quiet week of continued activity.
        val regenerated = !freezeAvailable &&
            freezeLastUsedDay != null &&
            todayEpochDay - freezeLastUsedDay >= FREEZE_REGENERATION_DAYS
        return StreakTransition(
            currentStreak = newStreak,
            longestStreak = maxOf(newStreak, longestStreak),
            freezeAvailable = freezeAvailable || regenerated,
            freezeLastUsedDay = freezeLastUsedDay,
            countedAsActive = true,
            freezeApplied = freezeApplied,
        )
    }

    private fun reset(longestStreak: Int) = StreakTransition(
        currentStreak = 1,
        longestStreak = longestStreak,
        freezeAvailable = true, // a fresh streak starts with its token back
        freezeLastUsedDay = null,
        countedAsActive = true,
        freezeApplied = false,
    )

    data class Milestone(val id: String, val emoji: String, val title: String)

    private val STREAK_MILESTONES = listOf(
        3 to Milestone("streak_3", "🔥", "3-day streak"),
        7 to Milestone("streak_7", "⚡", "7-day streak"),
        14 to Milestone("streak_14", "🌟", "14-day streak"),
        30 to Milestone("streak_30", "👑", "30-day streak"),
        100 to Milestone("streak_100", "🏆", "100-day streak"),
    )
    private val XP_MILESTONES = listOf(
        50 to Milestone("xp_50", "🌱", "50 XP"),
        200 to Milestone("xp_200", "🌳", "200 XP"),
        500 to Milestone("xp_500", "🏔️", "500 XP"),
    )
    private val LESSON_MILESTONES = listOf(
        1 to Milestone("lesson_1", "🎉", "First lesson passed"),
        5 to Milestone("lesson_5", "🚌", "5 lessons passed"),
        20 to Milestone("lesson_20", "🎓", "20 lessons passed"),
    )

    /** Every milestone reached at these numbers, oldest-first. Deterministic. */
    fun milestonesFor(streakDays: Int, totalXp: Int, lessonsPassed: Int): List<Milestone> =
        buildList {
            addAll(STREAK_MILESTONES.filter { it.first <= streakDays }.map { it.second })
            addAll(XP_MILESTONES.filter { it.first <= totalXp }.map { it.second })
            addAll(LESSON_MILESTONES.filter { it.first <= lessonsPassed }.map { it.second })
        }

    /** Lifetime counters milestones are computed from. */
    data class Totals(val streakDays: Int, val totalXp: Int, val lessonsPassed: Int)

    /** Milestones present in [after] but not [before] — oldest-first; a regression yields none. */
    fun newlyReached(before: Totals, after: Totals): List<Milestone> {
        val beforeIds = milestonesFor(before.streakDays, before.totalXp, before.lessonsPassed)
            .mapTo(mutableSetOf()) { it.id }
        return milestonesFor(after.streakDays, after.totalXp, after.lessonsPassed)
            .filter { it.id !in beforeIds }
    }

    private val MILESTONES_BY_ID: Map<String, Milestone> =
        (STREAK_MILESTONES + XP_MILESTONES + LESSON_MILESTONES).associate { it.second.id to it.second }

    fun milestoneById(id: String): Milestone? = MILESTONES_BY_ID[id]
}
