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
    val freezeAvailable: Boolean = true,  // 1 freeze per 7 days
    val totalDaysLearned: Int = 0
)
