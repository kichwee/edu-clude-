package com.example.educloud.data.local

import androidx.room.*
import com.example.educloud.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?

    @Query("SELECT * FROM students WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getStudentByDeviceId(deviceId: String): Student?

    @Query("SELECT * FROM students ORDER BY lastActive DESC LIMIT 1")
    fun getActiveStudent(): Flow<Student?>

    @Query("UPDATE students SET lastActive = :timestamp WHERE id = :id")
    suspend fun updateLastActive(id: Int, timestamp: Long = System.currentTimeMillis())
}
