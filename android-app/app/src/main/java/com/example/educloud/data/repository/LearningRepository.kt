package com.example.educloud.data.repository

import com.example.educloud.data.local.InteractionDao
import com.example.educloud.data.local.StreakDao
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.model.Streak
import com.example.educloud.habit.HabitEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.ZoneId

/** What the Progress screen may truthfully claim about a rolling week (plan §8). */
data class WeeklyLearningStats(
    val answersThisWeek: Int = 0,
    val minutesLearnedThisWeek: Long = 0,
    val topicsTouchedThisWeek: Int = 0,
)

class LearningRepository(
    private val interactionDao: InteractionDao,
    private val streakDao: StreakDao
) {
    fun getRecentInteractions(studentId: Int): Flow<List<Interaction>> =
        interactionDao.getRecentInteractions(studentId)

    fun getStreak(studentId: Int): Flow<Streak?> =
        streakDao.getStreak(studentId)

    suspend fun getStreakOnce(studentId: Int): Streak? =
        streakDao.getStreakOnce(studentId)

    suspend fun recordInteraction(interaction: Interaction): Long =
        interactionDao.insertInteraction(interaction)

    /**
     * Updates the streak after a learning session via [HabitEngine] (D10):
     * one missed day consumes an automatic freeze token; longer gaps reset.
     * Same-day repeats and rolled-back clocks leave the stored row untouched.
     */
    suspend fun updateStreak(studentId: Int) {
        val now = System.currentTimeMillis()
        val existing = streakDao.getStreakOnce(studentId)

        if (existing == null) {
            streakDao.insertOrUpdate(
                Streak(
                    studentId = studentId,
                    currentStreak = 1,
                    longestStreak = 1,
                    lastActivityDate = now,
                    totalDaysLearned = 1,
                )
            )
            return
        }

        val transition = HabitEngine.applyDailyActivity(
            currentStreak = existing.currentStreak,
            longestStreak = existing.longestStreak,
            lastActiveEpochDay = existing.lastActivityDate?.let { epochDayOf(it) },
            todayEpochDay = epochDayOf(now),
            freezeAvailable = existing.freezeAvailable,
            freezeLastUsedDay = existing.freezeLastUsedDay,
        )

        // countedAsActive=false covers both same-day repeats and a clock set
        // backward: in neither case may counters move or history rewind.
        if (!transition.countedAsActive) return

        streakDao.applyTransition(
            studentId = studentId,
            currentStreak = transition.currentStreak,
            longestStreak = transition.longestStreak,
            lastActivityDate = now,
            freezeAvailable = transition.freezeAvailable,
            freezeLastUsedDay = transition.freezeLastUsedDay,
            daysAdded = 1,
        )
    }

    /** Mastery-only XP (D11). Pass only HabitEngine.XpEvent values. */
    suspend fun addMasteryXp(studentId: Int, event: HabitEngine.XpEvent) {
        when (event) {
            HabitEngine.XpEvent.LESSON_PASSED ->
                streakDao.addXpAndLessonPassed(studentId, HabitEngine.xpFor(event))
            else -> streakDao.addXp(studentId, HabitEngine.xpFor(event))
        }
    }

    suspend fun getInteractionsBySubject(studentId: Int, subject: String): List<Interaction> =
        interactionDao.getInteractionsBySubject(studentId, subject)

    /**
     * Room-derived weekly stats. Answers count every recorded interaction; time
     * excludes interactions with no measured duration; topics need a named strand.
     * Zeroes mean "not enough evidence", never placeholders (plan §8).
     */
    fun observeWeeklyStats(studentId: Int, sinceMillis: Long): Flow<WeeklyLearningStats> =
        combine(
            interactionDao.observeInteractionsSince(studentId, sinceMillis),
            interactionDao.observeTimeLearnedSince(studentId, sinceMillis),
            interactionDao.observeTopicsSince(studentId, sinceMillis),
        ) { answers, timeTakenMs, topics ->
            WeeklyLearningStats(
                answersThisWeek = answers,
                minutesLearnedThisWeek = (timeTakenMs ?: 0L) / MILLIS_PER_MINUTE,
                topicsTouchedThisWeek = topics,
            )
        }

    fun observeQuizAnswersSince(studentId: Int, sinceMillis: Long): Flow<Int> =
        interactionDao.observeQuizAnswersSince(studentId, sinceMillis)

    suspend fun getPendingSyncInteractions(studentId: Int): List<Interaction> =
        interactionDao.getPendingSyncInteractions(studentId)

    companion object {
        private const val MILLIS_PER_MINUTE = 60_000L

        /**
         * Calendar-date epoch day in [zone]. Deliberately not a millis division:
         * local-midnight arithmetic miscounts DST weekends where the UTC offset
         * crosses zero (phantom gap==2 burns freeze tokens; spring-forward
         * collapses two days into one). minSdk 26 ships java.time.
         */
        internal fun epochDayOf(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
            Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toEpochDay()

        /** Inclusive start of the learner's local calendar day. */
        internal fun startOfLocalDayMillis(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
            Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
