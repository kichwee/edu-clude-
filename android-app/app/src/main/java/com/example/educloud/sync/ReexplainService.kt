package com.example.educloud.sync

import com.example.educloud.BuildConfig
import com.example.educloud.ai.ModelExplanation
import com.example.educloud.ai.ModelExplanationPolicy
import com.example.educloud.content.ContentLesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/** The bounded analogy-domain list; mirrors ``AnalogyDomain`` on the Django side. */
val ANALOGY_DOMAINS = listOf(
    "sports",
    "animals",
    "music",
    "transport",
    "food",
    "games",
    "nature",
    "money",
)

const val MAX_INTEREST_CHIPS = 3

val INTEREST_CHIP_LABELS = mapOf(
    "sports" to "⚽ Sports",
    "animals" to "🐘 Animals",
    "music" to "🥁 Music",
    "transport" to "🚌 Transport",
    "food" to "🥭 Food",
    "games" to "🎲 Games",
    "nature" to "🌦️ Nature",
    "money" to "🪙 Money",
)
const val MAX_SOURCE_EXCERPT_CHARS = 600
const val MAX_LEARNER_QUESTION_CHARS = 240

/** Keeps chip writes inside the server contract and the three-chip interest cap. */
fun sanitizeInterestDomains(raw: Iterable<String>): List<String> =
    raw.map(String::trim).filter { it in ANALOGY_DOMAINS }.distinct().take(MAX_INTEREST_CHIPS)

@Serializable
data class ReexplainRequestPayload(
    @SerialName("consent") val consent: Boolean,
    @SerialName("learner_id") val learnerId: String,
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("analogy_domain") val analogyDomain: String,
    @SerialName("source_excerpt") val sourceExcerpt: String,
    @SerialName("verified_answer") val verifiedAnswer: String? = null,
    @SerialName("learner_question") val learnerQuestion: String? = null,
)

@Serializable
data class ReexplainResponsePayload(
    val status: String,
    @SerialName("lesson_id") val lessonId: String,
    val explanation: String,
    @SerialName("preserves_verified_answer") val preservesVerifiedAnswer: Boolean = false,
    val cached: Boolean = false,
    val provider: String? = null,
)

sealed interface ReexplainOutcome {
    /** No usable endpoint configured; the tutor stays fully local. */
    data object Disabled : ReexplainOutcome
    data class Ready(val payload: ReexplainResponsePayload) : ReexplainOutcome
    /** A 400-class rejection, or a response that violates the strict shape. */
    data object Rejected : ReexplainOutcome
    /** Service off (404), throttled (429), provider failure (5xx). */
    data object Unavailable : ReexplainOutcome
    /** Transport failure; the deterministic explanation covers meanwhile. */
    data object Retry : ReexplainOutcome
}

/** One tap, one bounded call to ``POST /api/v1/tutor/reexplain``, mirroring [CloudSyncRepository]'s conventions. */
class ReexplainService(baseUrl: String = BuildConfig.REEXPLAIN_BASE_URL) {

    /** Production endpoints must be https; only loopback/emulator dev hosts may be cleartext. */
    private val endpointBase = normalizeEndpoint(baseUrl)

    suspend fun reexplain(request: ReexplainRequestPayload): ReexplainOutcome = withContext(Dispatchers.IO) {
        val base = endpointBase ?: return@withContext ReexplainOutcome.Disabled
        try {
            val response = post("$base/api/v1/tutor/reexplain", json.encodeToString(ReexplainRequestPayload.serializer(), request))
            when (response.code) {
                in 200..299 -> parseReady(response.body)
                HttpURLConnection.HTTP_BAD_REQUEST -> ReexplainOutcome.Rejected
                else -> ReexplainOutcome.Unavailable
            }
        } catch (_: IOException) {
            ReexplainOutcome.Retry
        } catch (_: SerializationException) {
            ReexplainOutcome.Rejected
        }
    }

    private fun parseReady(body: String): ReexplainOutcome =
        runCatching { json.decodeFromString(ReexplainResponsePayload.serializer(), body) }
            .map { payload ->
                if (payload.status == "ready") ReexplainOutcome.Ready(payload) else ReexplainOutcome.Unavailable
            }
            .getOrElse { ReexplainOutcome.Rejected }

    private fun post(url: String, body: String): HttpResponse {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = NETWORK_TIMEOUT_MS
            connection.readTimeout = NETWORK_TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.bufferedWriter(StandardCharsets.UTF_8).use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            HttpResponse(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    private data class HttpResponse(val code: Int, val body: String)

    private companion object {
        const val NETWORK_TIMEOUT_MS = 15_000
        val LOOPBACK_HTTP = Regex("""^http://(localhost|127\.0\.0\.1|10\.0\.2\.2)(:\d+)?${'$'}""")
        val json = Json { ignoreUnknownKeys = false; explicitNulls = false }

        fun normalizeEndpoint(baseUrl: String): String? {
            val trimmed = baseUrl.trim().trimEnd('/')
            return when {
                trimmed.startsWith("https://") -> trimmed
                trimmed.matches(LOOPBACK_HTTP) -> trimmed // local Django dev server only
                else -> null
            }
        }
    }
}

/**
 * Applies the shipped [ModelExplanationPolicy] to a server re-explanation.
 * The policy remains the single untrusted-output gate on Android: server-side
 * validation is necessary but never sufficient, so every cloud explanation is
 * re-checked against the local canonical lesson before it reaches a child.
 */
fun validatedServerExplanation(payload: ReexplainResponsePayload?, lesson: ContentLesson): Result<String> {
    val candidate = payload ?: return Result.failure(IllegalArgumentException("No re-explanation was returned."))
    return ModelExplanationPolicy.validate(
        candidate = ModelExplanation(
            explanation = payload.explanation,
            sourceId = payload.lessonId,
            usesOnlySource = payload.status == "ready" && payload.preservesVerifiedAnswer,
        ),
        lesson = lesson,
        requireVerifiedAnswer = lesson.verifiedAnswer != null,
    )
}
