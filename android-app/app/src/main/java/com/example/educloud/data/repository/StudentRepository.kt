package com.example.educloud.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.educloud.data.local.StudentDao
import com.example.educloud.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "edu_prefs")

class StudentRepository(
    private val studentDao: StudentDao,
    private val context: Context
) {
    companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_STUDENT_ID = intPreferencesKey("student_id")
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    val currentStudentId: Flow<Int?> = context.dataStore.data.map { prefs ->
        prefs[KEY_STUDENT_ID]
    }

    val activeStudent: Flow<Student?> = studentDao.getActiveStudent()
    var hasCheckedInitialStudent: Boolean = false
        private set

    init {
        // Simple flag to indicate we've at least started collecting
        // In a real app you might use a StateFlow for more robust initial state loading
        hasCheckedInitialStudent = true
    }

    suspend fun createStudent(alias: String, grade: Int, languagePref: String): Student {
        val deviceId = generateDeviceId(context)
        val student = Student(
            deviceId = deviceId,
            alias = alias,
            grade = grade,
            languagePref = languagePref
        )
        val id = studentDao.insertStudent(student).toInt()
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = true
            prefs[KEY_STUDENT_ID] = id
        }
        return student.copy(id = id)
    }

    suspend fun getStudentById(id: Int): Student? = studentDao.getStudentById(id)

    suspend fun markActive(studentId: Int) {
        studentDao.updateLastActive(studentId)
    }

    private fun generateDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("device", Context.MODE_PRIVATE)
        return prefs.getString("device_id", null) ?: run {
            val id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", id).apply()
            id
        }
    }
}
