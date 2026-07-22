package com.example.educloud.ai

import com.example.educloud.content.ContentLesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

/**
 * Live OpenAI API Client for EduCloud Grade 3 RAG Explainer.
 *
 * Checks API key validity and performs source-bounded RAG generation using
 * retrieved Grade 3 textbook passages as context.
 */
object OpenAiTutorService {

    private fun getApiKey(): String? {
        val envKey = System.getenv("OPENAI_API_KEY")
        if (!envKey.isNull_blank()) return envKey
        val propKey = System.getProperty("OPENAI_API_KEY")
        if (!propKey.isNull_blank()) return propKey
        return null
    }

    private fun String?.isNull_blank(): Boolean = this == null || this.isBlank()

    val isConfigured: Boolean
        get() = !getApiKey().isNull_blank()

    /**
     * Verifies whether the configured OpenAI API key is active and working.
     */
    suspend fun checkApiHealth(): Boolean = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext false
        try {
            val url = URL("https://api.openai.com/v1/models")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 4000
                readTimeout = 4000
            }
            val responseCode = conn.responseCode
            conn.disconnect()
            responseCode in 200..299
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Generates a warm, Grade 3 child-engaging explanation using the OpenAI API
     * grounded in the retrieved textbook passage.
     */
    suspend fun generateExplanation(
        question: String,
        lesson: ContentLesson,
        mode: LlmTutorExplainer.ExplainerMode = LlmTutorExplainer.ExplainerMode.EXPLAIN,
        deterministicAnswer: String? = null,
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext null
        try {
            val prompt = LlmTutorExplainer.generatePrompt(question, lesson, mode, deterministicAnswer)
            val url = URL("https://api.openai.com/v1/chat/completions")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val payload = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are a warm, encouraging Grade 3 Maths teacher in Kenya. Provide clear, detailed explanations grounded strictly in the provided lesson context. Do NOT just output a page number. If a verified answer is provided, use it exactly. Always cite the book page numbers at the end of your explanation.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("max_tokens", 180)
                put("temperature", 0.7)
            }

            conn.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val json = JSONObject(responseText)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    val content = message?.optString("content")?.trim()
                    if (!content.isNull_blank()) {
                        return@withContext "$content\n\n✨ Powered by OpenAI Cloud LLM • Source: ${lesson.source}"
                    }
                }
            }
            conn.disconnect()
            null
        } catch (_: Exception) {
            null
        }
    }
}
