package com.example.educloud.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Tracks daily learning streaks per student.
 * Mirrors PRD streaks table — used to trigger milestone notifications (7/14/30/90/365 days).
 */
@Entity(
    tableName = "streaks",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Streak(
    @PrimaryKey
    val studentId: Int,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long? = null,   // epoch millis of last interaction day
    val freezeAvailable: Boolean = true,  // 1 auto-freeze token per rolling week (D10)
    val totalDaysLearned: Int = 0,
    val totalXp: Long = 0,                // mastery-only XP; never raw activity (D11)
    val lessonsPassed: Int = 0,           // lifetime lessons with a perfect quick-check (D11)
    val freezeLastUsedDay: Long? = null,  // epoch-day the token was consumed
)
