package com.example.data.api

/**
 * Pure helpers for Gemini API key handling.
 * No Android dependencies so the key-entry logic can be unit-tested.
 */
object ApiKeyUtils {

    /** BuildConfig placeholder value used when no key was baked in at build time. */
    const val BUILD_PLACEHOLDER = "MY_GEMINI_API_KEY"

    /**
     * Normalize a user-entered key: trim, strip one pair of surrounding
     * quotes, and remove ALL whitespace (keys never contain spaces, so any
     * whitespace is a copy-paste artifact).
     */
    fun sanitize(raw: String): String =
        raw.trim().removeSurrounding("\"").removeSurrounding("'").filterNot { it.isWhitespace() }

    /** Display-safe mask: first 4 + last 4 chars for long keys, never the full key. */
    fun mask(key: String): String = when {
        key.length >= 10 -> "${key.take(4)}••••••••${key.takeLast(4)}"
        key.isNotBlank() -> "••••••••"
        else -> "Not Configured"
    }

    /** True when the value is empty or the build-time placeholder (i.e. no real key). */
    fun isPlaceholderOrBlank(key: String?): Boolean =
        key.isNullOrBlank() || key == BUILD_PLACEHOLDER
}
