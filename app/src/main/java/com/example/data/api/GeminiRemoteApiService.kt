package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

data class GeminiContentRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

data class GeminiContentResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.8-live:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiContentRequest
    ): GeminiContentResponse
}

object GeminiClientProvider {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    @Volatile
    private var runtimeApiKey: String? = null
    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        prefs = context.applicationContext.getSharedPreferences("smart_remote_prefs", android.content.Context.MODE_PRIVATE)
        val saved = prefs?.getString("gemini_api_key", null)
        if (!saved.isNullOrBlank()) {
            runtimeApiKey = saved
        }
    }

    fun setRuntimeApiKey(key: String) {
        val sanitized = key.trim().removeSurrounding("\"").removeSurrounding("'").filterNot { it.isWhitespace() }
        runtimeApiKey = sanitized
        prefs?.edit()?.putString("gemini_api_key", sanitized)?.apply()
    }

    fun getApiKey(): String {
        val override = runtimeApiKey
        if (!override.isNullOrBlank()) {
            return override
        }
        val fromPrefs = prefs?.getString("gemini_api_key", null)
        if (!fromPrefs.isNullOrBlank()) {
            runtimeApiKey = fromPrefs
            return fromPrefs
        }
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key == "MY_GEMINI_API_KEY" || key.isBlank()) "" else key
        } catch (_: Exception) {
            ""
        }
    }

    fun extractErrorMessage(e: retrofit2.HttpException): String {
        val errorJson = try {
            e.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        }

        if (!errorJson.isNullOrBlank()) {
            try {
                val jsonObject = JSONObject(errorJson)
                val errorObj = jsonObject.optJSONObject("error")
                val message = errorObj?.optString("message") ?: ""
                val details = errorObj?.optJSONArray("details")
                var reason = ""
                if (details != null) {
                    for (i in 0 until details.length()) {
                        val d = details.optJSONObject(i)
                        val r = d?.optString("reason")
                        if (!r.isNullOrBlank()) {
                            reason = r
                            break
                        }
                    }
                }

                if (message.contains("User location is not supported", ignoreCase = true)) {
                    return "Google Gemini API သည် လက်ရှိဒေသမှ တိုက်ရိုက်ခေါ်ယူမှုကို ကန့်သတ်ထားပါသည် (User location not supported)။ ကျေးဇူးပြု၍ ဖုန်းတွင် VPN ကို ဖွင့်ပြီး ပြန်လည် စမ်းသပ်ပေးပါခင်ဗျာ။"
                }

                if (reason == "API_KEY_INVALID" || message.contains("API key not valid", ignoreCase = true)) {
                    return "ထည့်သွင်းထားသော Gemini API Key မမှန်ကန်ပါ (API Key Invalid)။ Google AI Studio (aistudio.google.com) မှ API Key အစစ်အမှန်ကို ပြန်လည်စစ်ဆေးပေးပါခင်ဗျာ။"
                }

                if (reason == "ACCESS_TOKEN_TYPE_UNSUPPORTED" || e.code() == 401) {
                    return "API Key ပုံစံ မှားယွင်းနေပါသည် (HTTP 401 Unauthorized)။ API Key စာသား အပြည့်အစုံ ပါဝင်ခြင်း ရှိ/မရှိ စစ်ဆေးပေးပါခင်ဗျာ။"
                }

                if (message.isNotBlank()) {
                    return message
                }
            } catch (_: Exception) {
                // fallback below
            }
        }

        return when (e.code()) {
            400 -> "API Key သို့မဟုတ် Request ပုံစံ မှားယွင်းနေပါသည် (HTTP 400 Bad Request)"
            401 -> "API Key ခွင့်ပြုချက် မရရှိပါ (HTTP 401 Unauthorized)"
            403 -> "API Key သက်တမ်းကုန်ဆုံးသွားခြင်း သို့မဟုတ် ခွင့်ပြုချက်မရှိခြင်း (HTTP 403 Forbidden)"
            404 -> "Gemini Model Endpoint မတွေ့ပါ (HTTP 404)"
            429 -> "API Quota ကုန်သွားပါပြီ (HTTP 429 Quota Exceeded)"
            else -> "HTTP Error ${e.code()}: ${e.message()}"
        }
    }

    suspend fun validateApiKey(keyToTest: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val key = keyToTest?.trim()?.removeSurrounding("\"")?.removeSurrounding("'")?.filterNot { it.isWhitespace() }?.ifBlank { null } ?: getApiKey()

        if (key.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key ကွက်လပ်ဖြစ်နေပါသည် (Please enter API Key)"))
        }

        try {
            val testRequest = GeminiContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "ping")),
                        role = "user"
                    )
                )
            )
            val response = apiService.generateContent(key, testRequest)
            if (response.candidates != null) {
                Result.success("API Key မှန်ကန်ပြီး အသက်ဝင်နေပါသည် (Active & Valid)")
            } else {
                Result.failure(IllegalStateException("API Key တုံ့ပြန်မှု မရရှိပါ"))
            }
        } catch (e: retrofit2.HttpException) {
            val msg = extractErrorMessage(e)
            Result.failure(Exception(msg, e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
