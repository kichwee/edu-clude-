package com.example.educloud.data.repository

import com.example.educloud.data.local.InteractionDao
import com.example.educloud.data.local.StreakDao
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.model.Streak
import com.example.educloud.habit.HabitEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * JVM tests for the repository's streak bookkeeping using hand-rolled fakes —
 * no Room, no Android framework. Pins the honesty rules around epoch-day math.
 */
class LearningRepositoryTest {

    private class FakeStreakDao : StreakDao {
        val rows = mutableMapOf<Int, Streak>()

        override suspend fun insertOrUpdate(streak: Streak) {
            rows[streak.studentId] = streak
        }

        override fun getStreak(studentId: Int): Flow<Streak?> = flowOf(rows[studentId])

        override suspend fun getStreakOnce(studentId: Int): Streak? = rows[studentId]

        override suspend fun applyTransition(
            studentId: Int,
            currentStreak: Int,
            longestStreak: Int,
            lastActivityDate: Long,
            freezeAvailable: Boolean,
            freezeLastUsedDay: Long?,
            daysAdded: Int,
        ) {
            val existing = rows.getValue(studentId)
            rows[studentId] = existing.copy(
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                lastActivityDate = lastActivityDate,
                freezeAvailable = freezeAvailable,
                freezeLastUsedDay = freezeLastUsedDay,
                totalDaysLearned = existing.totalDaysLearned + daysAdded,
            )
        }

        override suspend fun addXp(studentId: Int, xp: Int) {
            val existing = rows.getValue(studentId)
            rows[studentId] = existing.copy(totalXp = existing.totalXp + xp)
        }

        override suspend fun addXpAndLessonPassed(studentId: Int, xp: Int) {
            val existing = rows.getValue(studentId)
            rows[studentId] = existing.copy(totalXp = existing.totalXp + xp, lessonsPassed = existing.lessonsPassed + 1)
        }
    }

    private class FakeInteractionDao : InteractionDao {
        override suspend fun insertInteraction(interaction: Interaction): Long = TODO("unused")
        override fun getRecentInteractions(studentId: Int, limit: Int): Flow<List<Interaction>> = TODO("unused")
        override suspend fun getInteractionsBySubject(studentId: Int, subject: String, limit: Int): List<Interaction> = TODO("unused")
        override suspend fun countInteractions(studentId: Int, subject: String, strand: String): Int = TODO("unused")
        override suspend fun getAverageDifficulty(studentId: Int, subject: String, strand: String, since: Long): Float? = TODO("unused")
        override suspend fun countInteractionsSince(studentId: Int, since: Long): Int = TODO("unused")
        override fun observeInteractionsSince(studentId: Int, since: Long): Flow<Int> = TODO("unused")
        override fun observeTimeLearnedSince(studentId: Int, since: Long): Flow<Long?> = TODO("unused")
        override fun observeTopicsSince(studentId: Int, since: Long): Flow<Int> = TODO("unused")
        override suspend fun getPendingSyncInteractions(studentId: Int): List<Interaction> = TODO("unused")
        override suspend fun getUnsyncedRegroupingAttempts(studentId: Int): List<Interaction> = TODO("unused")
        override suspend fun markRemediationSynced(interactionIds: List<Int>, syncedAt: Long) = TODO("unused")
    }

    private fun repoWith(initial: Streak): Pair<LearningRepository, FakeStreakDao> {
        val dao = FakeStreakDao()
        dao.rows[initial.studentId] = initial
        return Pair(LearningRepository(FakeInteractionDao(), dao), dao)
    }

    @Test
    fun `same-day repeat leaves the stored row untouched`() = runBlocking {
        val stored = Streak(
            studentId = 1,
            currentStreak = 5,
            longestStreak = 7,
            lastActivityDate = System.currentTimeMillis() - 2 * 60 * 60 * 1000, // earlier today
            totalDaysLearned = 30,
            totalXp = 55,
        )
        val (repo, dao) = repoWith(stored)

        repo.updateStreak(1)

        assertEquals(stored, dao.rows[1])
    }

    @Test
    fun `a rolled-back clock neither inflates counters nor rewinds history`() = runBlocking {
        val future = System.currentTimeMillis() + 48 * 60 * 60 * 1000 // device clock behind stored day
        val stored = Streak(
            studentId = 1,
            currentStreak = 9,
            longestStreak = 9,
            lastActivityDate = future,
            totalDaysLearned = 41,
            freezeAvailable = false,
            freezeLastUsedDay = 20_000,
        )
        val (repo, dao) = repoWith(stored)

        repo.updateStreak(1)

        assertEquals(stored, dao.rows[1])
    }

    @Test
    fun `epoch days stay consecutive across a fall-back DST weekend`() {
        val london = ZoneId.of("Europe/London")
        fun at(day: Int) = ZonedDateTime.of(2026, 10, day, 12, 0, 0, 0, london).toInstant().toEpochMilli()

        assertEquals(1L, LearningRepository.epochDayOf(at(26), london) - LearningRepository.epochDayOf(at(25), london))
        assertEquals(1L, LearningRepository.epochDayOf(at(25), london) - LearningRepository.epochDayOf(at(24), london))
    }

    @Test
    fun `epoch days stay consecutive across a spring-forward DST weekend`() {
        val london = ZoneId.of("Europe/London")
        fun at(day: Int) = ZonedDateTime.of(2026, 3, day, 12, 0, 0, 0, london).toInstant().toEpochMilli()

        assertEquals(1L, LearningRepository.epochDayOf(at(31), london) - LearningRepository.epochDayOf(at(30), london))
        assertEquals(1L, LearningRepository.epochDayOf(at(30), london) - LearningRepository.epochDayOf(at(29), london))
        assertEquals(1L, LearningRepository.epochDayOf(at(29), london) - LearningRepository.epochDayOf(at(28), london))
    }

    @Test
    fun `fixed-offset zones keep simple day arithmetic`() {
        val nairobi = ZoneId.of("Africa/Nairobi")
        fun at(hour: Long) = ZonedDateTime.of(2026, 8, 24, 0, 0, 0, 0, nairobi).toInstant().toEpochMilli() + hour * 3_600_000

        assertEquals(0L, LearningRepository.epochDayOf(at(23), nairobi) - LearningRepository.epochDayOf(at(0), nairobi))
    }
}
