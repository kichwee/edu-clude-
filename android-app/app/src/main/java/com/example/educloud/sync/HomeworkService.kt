package com.example.educloud.sync

import com.example.educloud.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.UUID

@Serializable
data class HomeworkAttemptPayload(
    @SerialName("learner_token") val learnerToken: String,
    val attempts: List<HomeworkAttemptRow>,
)

@Serializable
data class HomeworkAttemptRow(
    @SerialName("item_id") val itemId: String,
    val correct: Boolean,
)

sealed interface HomeworkFetchOutcome {
    data class Ready(val pack: HomeworkPack) : HomeworkFetchOutcome
    data object Disabled : HomeworkFetchOutcome
    data object Rejected : HomeworkFetchOutcome
    data object Unavailable : HomeworkFetchOutcome
}

sealed interface HomeworkSubmitOutcome {
    data object Submitted : HomeworkSubmitOutcome
    data object Disabled : HomeworkSubmitOutcome
    data object Rejected : HomeworkSubmitOutcome
    data object Unavailable : HomeworkSubmitOutcome
}

/**
 * Optional loopback/HTTPS client for the labelled homework-pack prototype.
 * The Android demo always has a bundled fixture; this only runs when a base URL is configured.
 */
class HomeworkService(baseUrl: String = BuildConfig.HOMEWORK_BASE_URL) {
    private val endpointBase = normalizeEndpoint(baseUrl)

    suspend fun fetchPack(classCode: String): HomeworkFetchOutcome = withContext(Dispatchers.IO) {
        val base = endpointBase ?: return@withContext HomeworkFetchOutcome.Disabled
        val code = normalizeClassCode(classCode)
        try {
            val response = get("$base/api/v1/demo/homework/$code")
            when (response.code) {
                in 200..299 -> parseHomeworkPack(response.body)
                    .fold(
                        onSuccess = { HomeworkFetchOutcome.Ready(it) },
                        onFailure = { HomeworkFetchOutcome.Rejected },
                    )
                HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_NOT_FOUND ->
                    HomeworkFetchOutcome.Rejected
                else -> HomeworkFetchOutcome.Unavailable
            }
        } catch (_: IOException) {
            HomeworkFetchOutcome.Unavailable
        }
    }

    suspend fun submitAttempts(
        pack: HomeworkPack,
        results: List<HomeworkItemResult>,
        learnerToken: String,
    ): HomeworkSubmitOutcome = withContext(Dispatchers.IO) {
        val base = endpointBase ?: return@withContext HomeworkSubmitOutcome.Disabled
        val token = uuidOrNull(learnerToken) ?: return@withContext HomeworkSubmitOutcome.Rejected
        val body = json.encodeToString(
            HomeworkAttemptPayload.serializer(),
            HomeworkAttemptPayload(
                learnerToken = token,
                attempts = results.map { HomeworkAttemptRow(it.itemId, it.correct) },
            ),
        )
        try {
            val response = post("$base/api/v1/demo/homework/${pack.classCode}/attempts", body)
            when (response.code) {
                in 200..299 -> HomeworkSubmitOutcome.Submitted
                HttpURLConnection.HTTP_BAD_REQUEST -> HomeworkSubmitOutcome.Rejected
                else -> HomeworkSubmitOutcome.Unavailable
            }
        } catch (_: IOException) {
            HomeworkSubmitOutcome.Unavailable
        }
    }

    private fun get(url: String): HttpResponse = exchange(url, method = "GET", body = null)

    private fun post(url: String, body: String): HttpResponse = exchange(url, method = "POST", body = body)

    private fun exchange(url: String, method: String, body: String?): HttpResponse {
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

    companion object {
        const val NETWORK_TIMEOUT_MS = 8_000
        private val LOOPBACK_HTTP = Regex("""^http://(localhost|127\.0\.0\.1|10\.0\.2\.2)(:\d+)?$""")
        private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

        fun normalizeEndpoint(baseUrl: String): String? {
            val trimmed = baseUrl.trim().trimEnd('/')
            return when {
                trimmed.startsWith("https://") -> trimmed
                trimmed.matches(LOOPBACK_HTTP) -> trimmed
                else -> null
            }
        }

        /** Null for anything that is not already a UUID: a learner token is never derived from a string. */
        fun uuidOrNull(raw: String): String? {
            return try {
                UUID.fromString(raw.trim()).toString()
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}
