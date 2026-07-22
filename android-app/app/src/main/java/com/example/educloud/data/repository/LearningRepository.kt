package com.example.educloud.data.repository

import com.example.educloud.data.local.InteractionDao
import com.example.educloud.data.local.StreakDao
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.model.Streak
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LearningRepository(
    private val interactionDao: InteractionDao,
    private val streakDao: StreakDao
) {
    fun getRecentInteractions(studentId: Int): Flow<List<Interaction>> =
        interactionDao.getRecentInteractions(studentId)

    fun getStreak(studentId: Int): Flow<Streak?> =
        streakDao.getStreak(studentId)

    suspend fun recordInteraction(interaction: Interaction): Long =
        interactionDao.insertInteraction(interaction)

    /**
     * Updates the streak after a learning session.
     * Logic: if last activity was yesterday → continue streak; if today → no-op; else → reset.
     */
    suspend fun updateStreak(studentId: Int) {
        val now = System.currentTimeMillis()
        val existing = streakDao.getStreakOnce(studentId)

        if (existing == null) {
            streakDao.insertOrUpdate(
                Streak(studentId = studentId, currentStreak = 1, longestStreak = 1, lastActivityDate = now, totalDaysLearned = 1)
            )
            return
        }

        val lastDate = existing.lastActivityDate ?: 0L
        val today = startOfDay(now)
        val yesterday = today - 86_400_000L
        val lastDay = startOfDay(lastDate)

        when {
            lastDay == today -> return // already logged today
            lastDay == yesterday -> {
                // Continue streak
                val newStreak = existing.currentStreak + 1
                val longest = maxOf(newStreak, existing.longestStreak)
                streakDao.updateStreak(studentId, newStreak, longest, now)
            }
            else -> {
                // Streak broken — reset
                streakDao.updateStreak(studentId, 1, existing.longestStreak, now)
            }
        }
    }

    suspend fun getInteractionsBySubject(studentId: Int, subject: String): List<Interaction> =
        interactionDao.getInteractionsBySubject(studentId, subject)

    suspend fun getPendingSyncInteractions(studentId: Int): List<Interaction> =
        interactionDao.getPendingSyncInteractions(studentId)

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
