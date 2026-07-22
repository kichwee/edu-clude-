package com.example.educloud.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RemediationLessonDao {
    @Query("SELECT * FROM remediation_lessons WHERE studentId = :studentId ORDER BY lessonId")
    suspend fun lessonsForStudent(studentId: Int): List<RemediationLessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<RemediationLessonEntity>)

    @Query("DELETE FROM remediation_lessons WHERE studentId = :studentId")
    suspend fun deleteForStudent(studentId: Int)

    @Transaction
    suspend fun replaceForStudent(studentId: Int, lessons: List<RemediationLessonEntity>) {
        deleteForStudent(studentId)
        insertAll(lessons)
    }
}
