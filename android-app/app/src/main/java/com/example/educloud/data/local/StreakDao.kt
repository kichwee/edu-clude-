package com.example.educloud.data.local

import androidx.room.*
import com.example.educloud.data.model.Streak
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(streak: Streak)

    @Query("SELECT * FROM streaks WHERE studentId = :studentId")
    fun getStreak(studentId: Int): Flow<Streak?>

    @Query("SELECT * FROM streaks WHERE studentId = :studentId")
    suspend fun getStreakOnce(studentId: Int): Streak?

    @Query(
        """
        UPDATE streaks SET
            currentStreak = :currentStreak,
            longestStreak = :longestStreak,
            lastActivityDate = :lastActivityDate,
            freezeAvailable = :freezeAvailable,
            freezeLastUsedDay = :freezeLastUsedDay,
            totalDaysLearned = totalDaysLearned + :daysAdded
        WHERE studentId = :studentId
        """
    )
    suspend fun applyTransition(
        studentId: Int,
        currentStreak: Int,
        longestStreak: Int,
        lastActivityDate: Long,
        freezeAvailable: Boolean,
        freezeLastUsedDay: Long?,
        daysAdded: Int,
    )

    /** Mastery-only XP (D11): callers may only pass HabitEngine.xpFor(event) values. */
    @Query("UPDATE streaks SET totalXp = totalXp + :xp WHERE studentId = :studentId")
    suspend fun addXp(studentId: Int, xp: Int)

    /** LESSON_PASSED pays XP and advances the lessons-passed counter in one statement. */
    @Query(
        """
        UPDATE streaks SET
            totalXp = totalXp + :xp,
            lessonsPassed = lessonsPassed + 1
        WHERE studentId = :studentId
        """
    )
    suspend fun addXpAndLessonPassed(studentId: Int, xp: Int)
}
