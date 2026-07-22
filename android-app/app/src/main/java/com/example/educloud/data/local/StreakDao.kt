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

    @Query("""
        UPDATE streaks SET 
            currentStreak = :currentStreak,
            longestStreak = :longestStreak,
            lastActivityDate = :lastActivityDate,
            totalDaysLearned = totalDaysLearned + 1
        WHERE studentId = :studentId
    """)
    suspend fun updateStreak(
        studentId: Int,
        currentStreak: Int,
        longestStreak: Int,
        lastActivityDate: Long
    )

    @Query("UPDATE streaks SET freezeAvailable = 0 WHERE studentId = :studentId")
    suspend fun useFreeze(studentId: Int)
}
