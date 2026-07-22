package com.example.educloud.sync

import com.example.educloud.BuildConfig
import com.example.educloud.data.local.InteractionDao
import com.example.educloud.data.local.RemediationLessonDao
import com.example.educloud.data.local.RemediationLessonEntity
import com.example.educloud.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Explicitly invoked synchronisation for the consented hackathon demo only. */
class CloudSyncRepository(
    private val interactionDao: InteractionDao,
    private val remediationLessonDao: RemediationLessonDao,
    private val baseUrl: String = BuildConfig.EDGE_SYNC_BASE_URL,
) {
    suspend fun sync(student: Student, requestId: String): CloudSyncOutcome = withContext(Dispatchers.IO) {
        val endpoint = normalizedEndpoint() ?: return@withContext CloudSyncOutcome.Disabled
        val interactions = interactionDao.getUnsyncedRegroupingAttempts(student.id)
        val attempts = interactions.mapNotNull(TelemetryMapper::toAttemptOrNull).take(MAX_ATTEMPTS)
        if (attempts.size < MINIMUM_ATTEMPTS) return@withContext CloudSyncOutcome.NotEnoughAttempts

        try {
            postTelemetry(endpoint, SyncTelemetryRequest(student.deviceId, requestId, true, attempts))
            val pack = downloadRemediation(endpoint, student.deviceId)
                ?: return@withContext CloudSyncOutcome.NoRemediation
            if (!pack.isSafeOfflinePatch()) return@withContext CloudSyncOutcome.RejectedPatch

            remediationLessonDao.replaceForStudent(
                student.id,
                pack.lessons.map { lesson ->
                    RemediationLessonEntity.fromDownloadedLesson(
                        studentId = student.id,
                        packId = pack.packId,
                        contentVersion = pack.contentVersion,
                        reviewStatus = pack.reviewStatus,
                        lesson = lesson,
                    )
                },
            )
            interactionDao.markRemediationSynced(interactions.map { it.id }, System.currentTimeMillis())
            CloudSyncOutcome.Downloaded(pack.lessons.size)
        } catch (_: IOException) {
            CloudSyncOutcome.Retry
        } catch (_: IllegalArgumentException) {
            CloudSyncOutcome.RejectedPatch
        }
    }

    private fun normalizedEndpoint(): String? = baseUrl.trim().trimEnd('/').takeIf { it.startsWith("https://") }

    private fun postTelemetry(endpoint: String, request: SyncTelemetryRequest) {
        val responseCode = request("$endpoint/api/v1/sync/telemetry", "POST", json.encodeToString(request))
        if (responseCode.code !in 200..299) throw IOException("Telemetry sync returned HTTP ${responseCode.code}")
    }

    private fun downloadRemediation(endpoint: String, learnerId: String): RemediationPack? {
        val query = URLEncoder.encode(learnerId, StandardCharsets.UTF_8.toString())
        val response = request("$endpoint/api/v1/sync/remediation?learner_id=$query", "GET")
        if (response.code == HttpURLConnection.HTTP_NO_CONTENT) return null
        if (response.code !in 200..299) throw IOException("Remediation download returned HTTP ${response.code}")
        return json.decodeFromString<RemediationDownloadResponse>(response.body).pack
    }

    private fun request(url: String, method: String, body: String? = null): HttpResponse {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        return try {
            connection.requestMethod = method
            connection.connectTimeout = NETWORK_TIMEOUT_MS
            connection.readTimeout = NETWORK_TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.bufferedWriter(StandardCharsets.UTF_8).use { it.write(body) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            HttpResponse(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    private data class HttpResponse(val code: Int, val body: String)

    private companion object {
        const val MINIMUM_ATTEMPTS = 3
        const val MAX_ATTEMPTS = 10
        const val NETWORK_TIMEOUT_MS = 15_000
        val json = Json { ignoreUnknownKeys = false; explicitNulls = false }
    }
}

sealed interface CloudSyncOutcome {
    data object Disabled : CloudSyncOutcome
    data object NotEnoughAttempts : CloudSyncOutcome
    data object NoRemediation : CloudSyncOutcome
    data object RejectedPatch : CloudSyncOutcome
    data object Retry : CloudSyncOutcome
    data class Downloaded(val lessonCount: Int) : CloudSyncOutcome
}

@Serializable
private data class SyncTelemetryRequest(
    @SerialName("learner_id") val learnerId: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("demo_consent") val demoConsent: Boolean,
    val attempts: List<SyncAttempt>,
)

@Serializable
internal data class SyncAttempt(
    @SerialName("attempt_id") val attemptId: String,
    @SerialName("skill_id") val skillId: String,
    val minuend: Int,
    val subtrahend: Int,
    @SerialName("learner_answer") val learnerAnswer: Int,
)

@Serializable
private data class RemediationDownloadResponse(val status: String, val pack: RemediationPack? = null)
