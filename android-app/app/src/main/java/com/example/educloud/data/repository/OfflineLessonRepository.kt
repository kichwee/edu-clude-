package com.example.educloud.data.repository

import com.example.educloud.content.ContentLesson
import com.example.educloud.content.Grade3MathContent
import com.example.educloud.data.local.LessonSearchDao
import com.example.educloud.data.local.LessonSearchEntity
import com.example.educloud.data.local.ContentPackStateDao
import com.example.educloud.data.local.ContentPackStateEntity
import com.example.educloud.data.local.RemediationLessonDao
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Seeds and queries the local lesson index. The only data source is the
 * bundled, provenance-labelled content pack; no learner question leaves the device.
 */
class OfflineLessonRepository(
    private val lessonSearchDao: LessonSearchDao,
    private val contentPackStateDao: ContentPackStateDao,
    private val remediationLessonDao: RemediationLessonDao? = null,
) {
    private val seedMutex = Mutex()

    suspend fun retrieve(question: String, subject: String, grade: Int, studentId: Int? = null): ContentLesson? {
        if (subject != Grade3MathContent.SUBJECT || grade != Grade3MathContent.GRADE) return null
        seedIfNeeded()
        val remediationCandidates = studentId?.let { id ->
            remediationLessonDao?.lessonsForStudent(id)?.map { it.toContentLesson() }.orEmpty()
        }.orEmpty()
        val matchQuery = LessonSearchQuery.build(question)
        val indexedCandidates = matchQuery?.let { query ->
            lessonSearchDao.search(query, subject, grade.toString(), limit = Grade3MathContent.lessons.size)
                .mapNotNull { entry -> Grade3MathContent.findById(entry.lessonId) }
        }.orEmpty()
        // FTS4 finds broad candidates quickly but its result order is not a teaching ranking.
        // Score the returned local records by their pack keywords before answering.
        return Grade3MathContent.find(question, remediationCandidates + indexedCandidates)
            ?: Grade3MathContent.find(question)
    }

    private suspend fun seedIfNeeded() = seedMutex.withLock {
        if (contentPackStateDao.versionFor() == Grade3MathContent.CONTENT_VERSION) return@withLock
        lessonSearchDao.deleteAll()
        lessonSearchDao.insertAll(
            Grade3MathContent.lessons.map { lesson ->
                LessonSearchEntity(
                    lessonId = lesson.id,
                    subject = Grade3MathContent.SUBJECT,
                    grade = Grade3MathContent.GRADE.toString(),
                    topic = lesson.topic,
                    searchableText = listOf(
                        lesson.topic,
                        lesson.keywords.joinToString(" "),
                        lesson.passage,
                        lesson.teachingSteps.joinToString(" "),
                    ).joinToString(" "),
                )
            }
        )
        contentPackStateDao.save(ContentPackStateEntity(version = Grade3MathContent.CONTENT_VERSION))
    }

}

/** Converts learner text to a bounded FTS prefix query without exposing FTS syntax. */
internal object LessonSearchQuery {
    fun build(question: String): String? {
        val tokens = question.lowercase()
            .split(Regex("[^a-z0-9]+"))
            // `"".all { ... }` is true in Kotlin, so reject empty fragments
            // before allowing one-digit Maths values such as "7".
            .filter { token -> token.isNotEmpty() && (token.length >= 2 || token.all(Char::isDigit)) }
            .take(6)
        return tokens.takeIf { it.isNotEmpty() }?.joinToString(" OR ") { "${it}*" }
    }
}
