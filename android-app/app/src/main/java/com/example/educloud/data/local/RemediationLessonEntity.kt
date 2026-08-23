package com.example.educloud.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.educloud.content.ContentLesson
import com.example.educloud.sync.RemediationLesson
import com.example.educloud.sync.RemediationPracticeQuestion
import com.example.educloud.sync.toContentLesson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Locally persisted, automatically validated remediation content for one learner profile. */
@Entity(
    tableName = "remediation_lessons",
    indices = [Index(value = ["studentId"]), Index(value = ["packId"])],
)
data class RemediationLessonEntity(
    @PrimaryKey val lessonId: String,
    val studentId: Int,
    val packId: String,
    val contentVersion: String,
    val validationStatus: String,
    val topic: String,
    val source: String,
    val keywordsJson: String,
    val microLesson: String,
    val teachingStepsJson: String,
    val definition: String,
    val practiceQuestionsJson: String,
) {
    fun toContentLesson(): ContentLesson = RemediationLesson(
        id = lessonId,
        topic = topic,
        source = source,
        keywords = json.decodeFromString(keywordsJson),
        microLesson = microLesson,
        teachingSteps = json.decodeFromString(teachingStepsJson),
        definition = definition,
        practiceQuestions = json.decodeFromString(practiceQuestionsJson),
    ).toContentLesson(contentVersion)

    companion object {
        private val json = Json { ignoreUnknownKeys = false }

        fun fromDownloadedLesson(
            studentId: Int,
            packId: String,
            contentVersion: String,
            validationStatus: String,
            lesson: RemediationLesson,
        ): RemediationLessonEntity = RemediationLessonEntity(
            lessonId = lesson.id,
            studentId = studentId,
            packId = packId,
            contentVersion = contentVersion,
            validationStatus = validationStatus,
            topic = lesson.topic,
            source = lesson.source,
            keywordsJson = json.encodeToString(lesson.keywords),
            microLesson = lesson.microLesson,
            teachingStepsJson = json.encodeToString(lesson.teachingSteps),
            definition = lesson.definition,
            practiceQuestionsJson = json.encodeToString(lesson.practiceQuestions),
        )
    }
}
