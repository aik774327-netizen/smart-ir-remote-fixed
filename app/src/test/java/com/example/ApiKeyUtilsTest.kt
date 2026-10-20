package com.example

import com.example.data.api.ApiKeyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeyUtilsTest {

    // ---------- sanitize ----------

    @Test
    fun sanitize_trimsSurroundingWhitespace() {
        assertEquals("AIzaSyD1234567890", ApiKeyUtils.sanitize("  AIzaSyD1234567890  "))
    }

    @Test
    fun sanitize_stripsSurroundingQuotes() {
        assertEquals("AIzaSyD1234567890", ApiKeyUtils.sanitize("\"AIzaSyD1234567890\""))
        assertEquals("AIzaSyD1234567890", ApiKeyUtils.sanitize("'AIzaSyD1234567890'"))
    }

    @Test
    fun sanitize_removesInternalWhitespaceFromPaste() {
        // Copy-paste artifacts: line breaks / spaces inside the key are removed.
        assertEquals("AIzaSyD1234567890", ApiKeyUtils.sanitize("AIzaSyD\n1234 567890"))
    }

    @Test
    fun sanitize_blankInputStaysBlank() {
        assertEquals("", ApiKeyUtils.sanitize(""))
        assertEquals("", ApiKeyUtils.sanitize("   "))
    }

    // ---------- mask ----------

    @Test
    fun mask_longKeyShowsFirstAndLastFourOnly() {
        assertEquals("AIza••••••••7890", ApiKeyUtils.mask("AIzaSyD1234567890"))
    }

    @Test
    fun mask_shortKeyIsFullyMasked() {
        assertEquals("••••••••", ApiKeyUtils.mask("abc123"))
    }

    @Test
    fun mask_emptyKeyShowsNotConfigured() {
        assertEquals("Not Configured", ApiKeyUtils.mask(""))
    }

    // ---------- isPlaceholderOrBlank ----------

    @Test
    fun placeholder_buildConfigPlaceholderIsDetected() {
        assertTrue(ApiKeyUtils.isPlaceholderOrBlank("MY_GEMINI_API_KEY"))
    }

    @Test
    fun placeholder_blankAndNullAreDetected() {
        assertTrue(ApiKeyUtils.isPlaceholderOrBlank(""))
        assertTrue(ApiKeyUtils.isPlaceholderOrBlank("   "))
        assertTrue(ApiKeyUtils.isPlaceholderOrBlank(null))
    }

    @Test
    fun placeholder_realKeyIsNotPlaceholder() {
        assertFalse(ApiKeyUtils.isPlaceholderOrBlank("AIzaSyD1234567890"))
    }
}
