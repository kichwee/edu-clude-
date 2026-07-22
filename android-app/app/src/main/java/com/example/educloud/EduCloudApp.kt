package com.example.educloud

import android.app.Application
import com.example.educloud.data.local.EduCloudDatabase
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.OfflineLessonRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.sync.CloudSyncRepository

/**
 * Application class — manual DI container (no Hilt to minimize APK size and compile time for MVP).
 * Provides singleton instances of database and repositories.
 */
class EduCloudApp : Application() {

    val database by lazy { EduCloudDatabase.getDatabase(this) }

    val studentRepository by lazy {
        StudentRepository(database.studentDao(), this)
    }

    val learningRepository by lazy {
        LearningRepository(database.interactionDao(), database.streakDao())
    }

    val offlineLessonRepository by lazy {
        OfflineLessonRepository(database.lessonSearchDao(), database.contentPackStateDao(), database.remediationLessonDao())
    }

    val cloudSyncRepository by lazy {
        CloudSyncRepository(database.interactionDao(), database.remediationLessonDao())
    }
}
