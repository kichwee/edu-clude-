package com.example.educloud.ai

import com.example.educloud.content.ContentLesson

/**
 * Boundary for optional local model experiments. It deliberately has no tool,
 * network, or document access: the caller supplies one retrieved lesson only.
 */
interface ExplanationProvider {
    suspend fun explain(
        lesson: ContentLesson,
        learnerQuestion: String,
        requireVerifiedAnswer: Boolean,
    ): Result<ModelExplanation>
}

/** The shipped provider is deliberately unavailable until a model passes evaluation. */
object DisabledExplanationProvider : ExplanationProvider {
    override suspend fun explain(
        lesson: ContentLesson,
        learnerQuestion: String,
        requireVerifiedAnswer: Boolean,
    ): Result<ModelExplanation> = Result.failure(
        IllegalStateException("On-device explanation is disabled until a model passes the quality gate."),
    )
}
