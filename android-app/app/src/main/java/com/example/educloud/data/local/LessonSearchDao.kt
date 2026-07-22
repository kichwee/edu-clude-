package com.example.educloud.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LessonSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonSearchEntity>)

    @Query("SELECT COUNT(*) FROM lesson_search")
    suspend fun count(): Int

    @Query("DELETE FROM lesson_search")
    suspend fun deleteAll()

    @Query(
        """
        SELECT * FROM lesson_search
        WHERE lesson_search MATCH :matchQuery
          AND subject = :subject
          AND grade = :grade
        LIMIT :limit
        """
    )
    suspend fun search(matchQuery: String, subject: String, grade: String, limit: Int): List<LessonSearchEntity>
}
