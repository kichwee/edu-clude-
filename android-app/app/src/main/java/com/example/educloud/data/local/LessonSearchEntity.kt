package com.example.educloud.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/** Local full-text index. It is bundled/seeded on device and never needs a network call. */
@Fts4
@Entity(tableName = "lesson_search")
data class LessonSearchEntity(
    @ColumnInfo(name = "lesson_id") val lessonId: String,
    val subject: String,
    val grade: String,
    val topic: String,
    @ColumnInfo(name = "searchable_text") val searchableText: String,
)
