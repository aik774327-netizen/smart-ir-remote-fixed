package com.kyawzay.blueconnect.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * DUPLICATE IMPLEMENTATION WARNING:
 * This is a duplicate of com.example.data.api.GeminiRemoteApiService.
 * Prefer the Retrofit-based implementation in com.example.data.api for new code.
 *
 * SECURITY NOTE: The API key is sent in the `x-goog-api-key` header, never
 * in the URL (URLs leak into logs/proxies). Never log the key value itself.
 */
class GeminiRemoteApiService(private val apiKey: String) {

    private val tag = "GeminiRemoteApi"

    suspend fun findIrProtocolFromAi(brand: String, model: String, button: String): String? = withContext(Dispatchers.IO) {
        val cleanKey = com.example.data.api.ApiKeyUtils.sanitize(apiKey)
        if (cleanKey.isBlank()) {
            Log.e(tag, "Gemini API Key is empty")
            return@withContext null
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-live:generateContent"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", cleanKey)
            conn.connectTimeout = 25000
            conn.readTimeout = 25000
            conn.doOutput = true

            val prompt = "Provide the 32-bit NEC IR Hex Code for $brand $model remote control button $button in format '0xXXXXXXXX'. Reply only with the hex code."
            val jsonBody = JSONObject().apply {
                put("contents", listOf(JSONObject().apply {
                    put("parts", listOf(JSONObject().apply {
                        put("text", prompt)
                    }))
                }))
            }

            conn.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidateObj = candidates.getJSONObject(0)
                    val contentObj = candidateObj.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

                    // Extract hex code matching 0x[0-9A-Fa-f]{6,8}
                    val hexRegex = "(0x[0-9A-Fa-f]{6,8})".toRegex()
                    val match = hexRegex.find(text)
                    return@withContext match?.value ?: text.lines().firstOrNull()?.trim()
                }
            } else {
                val errorStream = conn.errorStream
                val errorText = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    ""
                }
                Log.e(tag, "HTTP $responseCode Error: $errorText")

                if (errorText.contains("User location is not supported", ignoreCase = true)) {
                    throw IllegalStateException("Google Gemini API သည် လက်ရှိဒေသမှ တိုက်ရိုက်ခေါ်ယူမှုကို ကန့်သတ်ထားပါသည် (User location not supported)။ ကျေးဇူးပြု၍ ဖုန်းတွင် VPN ကို ဖွင့်ပေးပါခင်ဗျာ။")
                }
                if (errorText.contains("API_KEY_INVALID", ignoreCase = true) || errorText.contains("API key not valid", ignoreCase = true)) {
                    throw IllegalStateException("ထည့်သွင်းထားသော Gemini API Key မမှန်ကန်ပါ (API Key Invalid)။ ကျေးဇူးပြု၍ Google AI Studio မှ API Key အစစ်အမှန်ကို ထည့်သွင်းပေးပါခင်ဗျာ။")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Gemini API request failed: ${e.message}", e)
            throw e
        }
        return@withContext null
    }
}
