package com.example.educloud.ai

import android.content.Context
/**
 * Safe MVP stub for the experimental LiteRT-LM integration. The evaluated
 * baseline failed its quality gate, so the active app intentionally ships
 * without the native runtime or model route. The preserved experiment lives
 * outside this Gradle source set until a replacement passes evaluation.
 */
class OnDeviceTutorEngine(private val context: Context) {

    sealed interface Availability {
        data object Unavailable : Availability
        data object Ready : Availability
        data class Failed(val reason: String) : Availability
    }

    data class Response(
        val text: String,
        val elapsedMillis: Long,
    )

    fun availability(): Availability = Availability.Unavailable

    suspend fun explain(
        lessonTitle: String,
        retrievedFact: String,
        verifiedAnswer: String?,
        learnerQuestion: String,
    ): Result<Response> = Result.failure(
        IllegalStateException("The on-device baseline is disabled after its quality evaluation."),
    )
}
