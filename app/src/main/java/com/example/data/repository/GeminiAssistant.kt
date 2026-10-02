package com.example.data.repository

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiResponse(
    val explanation: String,
    val suggestedCode: String? = null,
    val targetFileName: String? = null
)

class GeminiAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun askAssistant(
        taskType: String, // "explain", "fix", "generate", "analyze_error"
        currentFileName: String,
        currentFileContent: String,
        userPrompt: String
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("Gemini API Key is not configured. Please add your key in the AI Studio Secrets panel.")
            )
        }

        try {
            val systemInstruction = """
                You are Web2APK AI, an expert mobile web & Android development assistant inside an Android IDE.
                The user is building HTML/CSS/JS websites and compiling them into Android WebView apps.
                When providing code fixes or new code, format your response as valid JSON with three keys:
                {
                  "explanation": "concise explanation of what was done or found",
                  "suggestedCode": "the complete replacement or new code if applicable, or null",
                  "targetFileName": "$currentFileName"
                }
                Only output the JSON object. Do not wrap in markdown quotes if possible, or use standard markdown JSON.
            """.trimIndent()

            val promptText = """
                Task: $taskType
                File Name: $currentFileName
                Current Content:
                ```
                ${currentFileContent.take(4000)}
                ```
                User Request: $userPrompt
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", promptText) })
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: ""
                    return@withContext Result.failure(Exception("Gemini API error (HTTP ${response.code}): $err"))
                }

                val body = response.body?.string() ?: ""
                val root = JSONObject(body)
                val candidates = root.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text") ?: ""

                // Parse structured JSON
                try {
                    val jsonResponse = JSONObject(text.trim())
                    val explanation = jsonResponse.optString("explanation", "Completed.")
                    val suggestedCode = if (jsonResponse.isNull("suggestedCode")) null else jsonResponse.optString("suggestedCode")
                    val targetFile = if (jsonResponse.isNull("targetFileName")) currentFileName else jsonResponse.optString("targetFileName")

                    Result.success(AiResponse(explanation, suggestedCode, targetFile))
                } catch (pe: Exception) {
                    // Fallback to raw text
                    Result.success(AiResponse(explanation = text, suggestedCode = null, targetFileName = currentFileName))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
