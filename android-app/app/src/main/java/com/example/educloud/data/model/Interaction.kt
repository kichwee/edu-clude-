package com.example.educloud.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records each AI tutoring interaction.
 * Mirrors PRD interactions table — tracks subject, difficulty, correctness for IRT calibration.
 */
@Entity(
    tableName = "interactions",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "subject"]),
        Index(value = ["channel", "createdAt"])
    ]
)
data class Interaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val question: String,
    val aiResponse: String,
    val ragSources: String? = null,        // JSON string of source citations
    val isCorrect: Boolean? = null,
    /** The selected quiz option, retained locally until a user-requested demo sync. */
    val learnerAnswer: String? = null,
    val timeTakenMs: Int? = null,
    val subject: String,                   // "math" | "science" | "kiswahili" | "english"
    val strand: String? = null,
    val difficulty: Float = 0.5f,
    val channel: String = "app",
    /** Set only after a learner explicitly requests the review-required demo sync. */
    val remediationSyncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
