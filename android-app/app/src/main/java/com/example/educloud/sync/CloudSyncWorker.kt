package com.example.educloud.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.educloud.BuildConfig
import com.example.educloud.EduCloudApp
import java.util.UUID

/** Runs only after the learner explicitly requests the consented demo sync. */
class CloudSyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val studentId = inputData.getInt(STUDENT_ID, NO_STUDENT)
        val requestId = inputData.getString(REQUEST_ID) ?: return Result.failure()
        val app = applicationContext as? EduCloudApp ?: return Result.failure()
        val student = app.studentRepository.getStudentById(studentId) ?: return Result.failure()
        return when (app.cloudSyncRepository.sync(student, requestId)) {
            CloudSyncOutcome.Retry -> Result.retry()
            else -> Result.success()
        }
    }

    companion object {
        private const val STUDENT_ID = "student_id"
        private const val REQUEST_ID = "request_id"
        private const val NO_STUDENT = -1

        fun isConfigured(): Boolean = BuildConfig.EDGE_SYNC_BASE_URL.trim().startsWith("https://")

        fun enqueue(context: Context, studentId: Int): Boolean {
            if (!isConfigured()) return false
            val work = OneTimeWorkRequestBuilder<CloudSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(workDataOf(STUDENT_ID to studentId, REQUEST_ID to UUID.randomUUID().toString()))
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "agentic-remediation-$studentId",
                ExistingWorkPolicy.REPLACE,
                work,
            )
            return true
        }
    }
}
