package com.example.educloud.ai

import android.app.ActivityManager
import android.content.Context
import com.example.educloud.content.ContentLesson
import com.example.educloud.content.Grade3MathContent
import com.example.educloud.content.Grade3MathRules
import com.example.educloud.content.Grade3NumericRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Creates grounded responses from the bundled lesson pack.
 *
 * This MVP does not bundle or run a local LLM. Naming the component after what
 * it actually does prevents the app and demo from making an unsupported model
 * claim. A future model implementation can conform to this same API.
 */
class TutorResponseEngine(private val context: Context) {

    companion object {
        private const val OOM_THRESHOLD_MB = 600L
        private const val STREAM_WORD_DELAY_MS = 60L
    }

    val isLowMemory: Boolean
        get() {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            return memoryInfo.availMem / (1024 * 1024) < OOM_THRESHOLD_MB
        }

    fun answer(
        subject: String,
        learnerQuestion: String,
        lesson: ContentLesson?,
        forceRetrievalOnly: Boolean = false,
        mode: LlmTutorExplainer.ExplainerMode = LlmTutorExplainer.ExplainerMode.EXPLAIN,
    ): Flow<String> = flow {
        val normalized = learnerQuestion.lowercase().trim()
        val isStoryRequest = normalized.contains("story") || mode == LlmTutorExplainer.ExplainerMode.STORY
        val isHintRequest = normalized.contains("hint") || mode == LlmTutorExplainer.ExplainerMode.HINT
        val isAnswerCheck = normalized.contains("correct") || normalized.contains("right answer") || normalized.startsWith("is ")

        val deterministicAnswer = lesson?.let { Grade3NumericRules.answerFor(it, learnerQuestion) }
            ?: Grade3MathRules.answerFor(learnerQuestion)
            ?: lesson?.verifiedAnswer

        val deterministicResponse = DeterministicTutor.respond(subject, learnerQuestion, lesson)

        val response = if (lesson != null && !isAnswerCheck) {
            val explainerMode = when {
                isStoryRequest -> LlmTutorExplainer.ExplainerMode.STORY
                isHintRequest -> LlmTutorExplainer.ExplainerMode.HINT
                else -> mode
            }
            if (OpenAiTutorService.isConfigured && !isLowMemory && !forceRetrievalOnly) {
                val rawLlm = OpenAiTutorService.generateExplanation(learnerQuestion, lesson, explainerMode, deterministicAnswer)
                if (rawLlm != null) {
                    val candidate = ModelExplanation(
                        explanation = rawLlm,
                        sourceId = lesson.id,
                        usesOnlySource = true,
                    )
                    val validated = ModelExplanationPolicy.validate(
                        candidate = candidate,
                        lesson = lesson,
                        requireVerifiedAnswer = lesson.verifiedAnswer != null,
                    )
                    if (validated.isSuccess) {
                        validated.getOrNull() ?: rawLlm
                    } else {
                        deterministicResponse
                    }
                } else {
                    LlmTutorExplainer.explainGrounded(learnerQuestion, lesson, explainerMode, deterministicAnswer)
                }
            } else {
                LlmTutorExplainer.explainGrounded(learnerQuestion, lesson, explainerMode, deterministicAnswer)
            }
        } else {
            deterministicResponse
        }

        val lowMemoryPrefix = if (isLowMemory || forceRetrievalOnly) {
            "Low-memory mode — using local Grade 3 source pack.\n\n"
        } else {
            ""
        }

        val buffer = StringBuilder()
        (lowMemoryPrefix + response).split(" ").forEach { word ->
            buffer.append(word).append(' ')
            emit(buffer.toString().trimEnd())
            delay(STREAM_WORD_DELAY_MS)
        }
    }.flowOn(Dispatchers.Default)

}
