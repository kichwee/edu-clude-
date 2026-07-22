// Preserved LiteRT-LM experiment. This directory is intentionally outside
// app/src and is not compiled or packaged by the Grade 3 Maths MVP.
package com.example.educloud.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OnDeviceTutorEngine(private val context: Context) {
    sealed interface Availability {
        data object Unavailable : Availability
        data object Ready : Availability
        data class Failed(val reason: String) : Availability
    }

    data class Response(val text: String, val elapsedMillis: Long)

    private val modelFile: File
        get() = File(context.filesDir, MODEL_RELATIVE_PATH)

    fun availability(): Availability =
        if (modelFile.isFile && modelFile.length() > 0L) Availability.Ready else Availability.Unavailable

    suspend fun explain(
        lessonTitle: String,
        retrievedFact: String,
        verifiedAnswer: String?,
        learnerQuestion: String,
    ): Result<Response> = withContext(Dispatchers.Default) {
        val file = modelFile
        if (!file.isFile || file.length() == 0L) {
            return@withContext Result.failure(IllegalStateException("The optional model pack is not installed."))
        }
        val startedAt = System.currentTimeMillis()
        runCatching {
            Engine(
                EngineConfig(
                    modelPath = file.absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = context.cacheDir.absolutePath,
                ),
            ).use { engine ->
                engine.initialize()
                engine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(SYSTEM_INSTRUCTION),
                        samplerConfig = SamplerConfig(topK = 10, topP = 0.9, temperature = 0.2),
                    ),
                ).use { conversation ->
                    val prompt = buildPrompt(lessonTitle, retrievedFact, verifiedAnswer, learnerQuestion)
                    val generatedText = conversation.sendMessage(prompt).contents.contents
                        .filterIsInstance<Content.Text>()
                        .joinToString(separator = "") { it.text }
                    Response(
                        text = generatedText.trim().take(MAX_RESPONSE_CHARACTERS),
                        elapsedMillis = System.currentTimeMillis() - startedAt,
                    )
                }
            }
        }
    }

    private fun buildPrompt(
        lessonTitle: String,
        retrievedFact: String,
        verifiedAnswer: String?,
        learnerQuestion: String,
    ): String = buildString {
        appendLine("Lesson: $lessonTitle")
        appendLine("Local lesson fact: $retrievedFact")
        verifiedAnswer?.let { appendLine("Verified answer: $it") }
        appendLine("Learner question: $learnerQuestion")
        append("Give a short explanation using only the local lesson fact and verified answer.")
    }

    private companion object {
        const val MODEL_RELATIVE_PATH = "models/smollm2-360m-instruct.litertlm"
        const val MAX_RESPONSE_CHARACTERS = 420
        const val SYSTEM_INSTRUCTION = """
            You are EduCloud's Grade 3 Maths explainer. Use simple English.
            Only explain the supplied local lesson fact and verified answer.
            Never invent facts, change a maths answer, mention the model, or answer unrelated questions.
            Keep the answer under 55 words.
        """
    }
}
