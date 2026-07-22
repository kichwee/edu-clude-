package com.example.educloud.data.repository

import com.example.educloud.content.Grade3MathContent
import com.example.educloud.data.local.ContentPackStateDao
import com.example.educloud.data.local.ContentPackStateEntity
import com.example.educloud.data.local.LessonSearchDao
import com.example.educloud.data.local.LessonSearchEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineLessonRepositoryIntegrationTest {

    @Test
    fun seedsAndRetrievesTheBundledCountingTwosLesson() = runBlocking {
        val index = FakeLessonSearchDao()
        val packState = FakeContentPackStateDao()
        val repository = OfflineLessonRepository(index, packState)

        val lesson = repository.retrieve("How do I count in twos?", "math", 3)

        assertEquals("g3-t1-w2-l1-counting-twos", lesson?.id)
        assertEquals(Grade3MathContent.lessons.size, index.rows.size)
        assertEquals(Grade3MathContent.CONTENT_VERSION, packState.version)
    }

    @Test
    fun replacesAnOlderSearchPackWithoutTouchingTheNewPackSelection() = runBlocking {
        val index = FakeLessonSearchDao(
            mutableListOf(
                LessonSearchEntity("obsolete", "math", "3", "Old lesson", "obsolete mangoes"),
            ),
        )
        val packState = FakeContentPackStateDao("old-pack")
        val repository = OfflineLessonRepository(index, packState)

        repository.retrieve("tens and ones", "math", 3)

        assertFalse(index.rows.any { it.lessonId == "obsolete" })
        assertEquals(Grade3MathContent.lessons.size, index.rows.size)
        assertEquals(Grade3MathContent.CONTENT_VERSION, packState.version)
    }

    private class FakeLessonSearchDao(
        val rows: MutableList<LessonSearchEntity> = mutableListOf(),
    ) : LessonSearchDao {
        override suspend fun insertAll(lessons: List<LessonSearchEntity>) {
            rows += lessons
        }

        override suspend fun count(): Int = rows.size

        override suspend fun deleteAll() {
            rows.clear()
        }

        override suspend fun search(
            matchQuery: String,
            subject: String,
            grade: String,
            limit: Int,
        ): List<LessonSearchEntity> {
            val terms = matchQuery.split(" OR ").map { it.removeSuffix("*") }
            return rows.filter { row ->
                row.subject == subject && row.grade == grade &&
                    terms.any { term -> row.searchableText.lowercase().contains(term) }
            }.take(limit)
        }
    }

    private class FakeContentPackStateDao(initialVersion: String? = null) : ContentPackStateDao {
        var version = initialVersion

        override suspend fun versionFor(id: Int): String? = version

        override suspend fun save(state: ContentPackStateEntity) {
            version = state.version
        }
    }
}
