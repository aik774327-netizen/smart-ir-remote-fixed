package com.example.data.api

import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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
import retrofit2.http.Header
import retrofit2.http.POST
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
        // Key travels in a header, never in the URL (URLs leak into logs/proxies).
        @Header("x-goog-api-key") apiKey: String,
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
            // Defense in depth: never print the API key header at any level.
            redactHeader("x-goog-api-key")
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

    private const val TAG = "GeminiClientProvider"
    private const val PREFS_NAME = "smart_remote_prefs"
    private const val SECURE_PREFS_NAME = "smart_remote_secure_prefs"
    private const val KEY_API_KEY = "gemini_api_key"

    @Volatile
    private var runtimeApiKey: String? = null
    private var prefs: android.content.SharedPreferences? = null
    private var securePrefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        val appContext = context.applicationContext
        prefs = appContext.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        securePrefs = try {
            // security-crypto 1.0.0 stable API (MasterKey.Builder exists only in 1.1.0+).
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                SECURE_PREFS_NAME,
                masterKeyAlias,
                appContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Keystore failure on some devices must never crash the app;
            // plain prefs remain as the fallback store in that case only.
            Log.e(TAG, "Encrypted storage unavailable, using plain prefs fallback", e)
            null
        }

        // Migration: move a legacy plain-text key into encrypted storage,
        // then delete the plain-text copy.
        val legacy = prefs?.getString(KEY_API_KEY, null)
        if (!legacy.isNullOrBlank() && securePrefs != null) {
            try {
                if (securePrefs?.getString(KEY_API_KEY, null).isNullOrBlank()) {
                    securePrefs?.edit()?.putString(KEY_API_KEY, legacy)?.apply()
                }
                prefs?.edit()?.remove(KEY_API_KEY)?.apply()
            } catch (e: Exception) {
                Log.e(TAG, "API key migration failed", e)
            }
        }

        val saved = readStoredKey()
        if (!saved.isNullOrBlank()) {
            runtimeApiKey = saved
        }
    }

    private fun readStoredKey(): String? {
        val secure = securePrefs
        if (secure != null) {
            return try {
                secure.getString(KEY_API_KEY, null)
            } catch (e: Exception) {
                Log.e(TAG, "Encrypted read failed", e)
                null
            }
        }
        // Plain fallback is read only when encryption is unavailable.
        return prefs?.getString(KEY_API_KEY, null)
    }

    private fun writeStoredKey(key: String) {
        val secure = securePrefs
        if (secure != null) {
            try {
                secure.edit().putString(KEY_API_KEY, key).apply()
                return
            } catch (e: Exception) {
                Log.e(TAG, "Encrypted save failed, using plain prefs fallback", e)
            }
        }
        prefs?.edit()?.putString(KEY_API_KEY, key)?.apply()
    }

    fun setRuntimeApiKey(key: String) {
        val sanitized = ApiKeyUtils.sanitize(key)
        runtimeApiKey = sanitized
        writeStoredKey(sanitized)
    }

    fun getApiKey(): String {
        val override = runtimeApiKey
        if (!override.isNullOrBlank()) {
            return override
        }
        val stored = readStoredKey()
        if (!stored.isNullOrBlank()) {
            runtimeApiKey = stored
            return stored
        }
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (ApiKeyUtils.isPlaceholderOrBlank(key)) "" else key
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
        val key = keyToTest?.let { ApiKeyUtils.sanitize(it) }?.ifBlank { null } ?: getApiKey()

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
