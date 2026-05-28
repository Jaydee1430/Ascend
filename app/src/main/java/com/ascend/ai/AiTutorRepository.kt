package com.ascend.ai

import com.ascend.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AiTutorMessage(
    val role: String,
    val text: String
)

class AiTutorRepository(
    private val proxyEndpoint: String = BuildConfig.AI_TUTOR_PROXY_URL,
    private val geminiApiKey: String = BuildConfig.GEMINI_API_KEY,
    private val geminiModel: String = BuildConfig.GEMINI_MODEL
) {
    suspend fun askStudyQuestion(messages: List<AiTutorMessage>): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            when {
                proxyEndpoint.isNotBlank() -> callProxy(messages)
                geminiApiKey.isNotBlank() -> callGeminiDirectly(messages)
                else -> throw IllegalStateException(
                    "Try Again"

                )
            }
        }
    }

    private fun callProxy(messages: List<AiTutorMessage>): String {
        val response = postJson(
            url = proxyEndpoint,
            body = buildProxyRequest(messages)
        )
        return extractReplyText(response)
    }

    private fun callGeminiDirectly(messages: List<AiTutorMessage>): String {
        val model = geminiModel.removePrefix("models/")
        val response = postJson(
            url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent",
            body = buildGeminiRequest(messages),
            headers = mapOf("x-goog-api-key" to geminiApiKey)
        )
        return extractReplyText(response)
    }

    private fun postJson(
        url: String,
        body: JSONObject,
        headers: Map<String, String> = emptyMap()
    ): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
        }

        try {
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (statusCode !in 200..299) {
                throw IOException("AI request failed ($statusCode): ${responseText.take(220)}")
            }

            if (responseText.isBlank()) {
                throw IOException("AI service returned an empty response.")
            }

            return JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun buildProxyRequest(messages: List<AiTutorMessage>): JSONObject =
        JSONObject().apply {
            put("systemInstruction", SYSTEM_INSTRUCTION)
            put("messages", JSONArray().apply {
                messages.forEach { message ->
                    put(JSONObject().apply {
                        put("role", message.role)
                        put("text", message.text)
                    })
                }
            })
        }

    private fun buildGeminiRequest(messages: List<AiTutorMessage>): JSONObject =
        JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_INSTRUCTION)))
            })
            put("contents", JSONArray().apply {
                messages.forEach { message ->
                    put(JSONObject().apply {
                        put("role", message.role)
                        put("parts", JSONArray().put(JSONObject().put("text", message.text)))
                    })
                }
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 1024)
            })
        }

    private fun extractReplyText(response: JSONObject): String {
        response.optString("reply").takeIf { it.isNotBlank() }?.let { return it }
        response.optString("text").takeIf { it.isNotBlank() }?.let { return it }
        response.optString("message").takeIf { it.isNotBlank() }?.let { return it }

        val candidates = response.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val parts = candidates
                .optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")

            if (parts != null) {
                val text = buildString {
                    for (index in 0 until parts.length()) {
                        val partText = parts.optJSONObject(index)?.optString("text").orEmpty()
                        if (partText.isNotBlank()) {
                            append(partText)
                        }
                    }
                }.trim()

                if (text.isNotBlank()) {
                    return text
                }
            }
        }

        val apiError = response.optJSONObject("error")?.optString("message").orEmpty()
        if (apiError.isNotBlank()) {
            throw IOException(apiError)
        }

        throw IOException("AI service returned a response without a reply.")
    }

    companion object {
        const val SYSTEM_INSTRUCTION = """
You are Beru, an AI study assistant inside the Arise study app.

Your purpose is to answer basic student inquiries about school topics in a simple, clear, and helpful way.

Rules:

* Explain topics in a beginner-friendly way.
* Keep answers short but useful.
* Prefer compact, complete answers that finish cleanly.
* Use examples when helpful.
* If the student is confused, explain step by step.
* If the student asks for homework answers, help them understand the concept instead of only giving the final answer.
* Do not claim that you can access the user's flashcards, notes, files, or study progress.
* Do not mention that you are connected to flashcard sets, because you are not.
* If the question is not related to studying or learning, politely say that you are designed to help with study-related questions only.
* Avoid very long explanations unless the student asks for more detail.
* Use a friendly and supportive tone.
"""
    }
}
