package com.jakebarnby.stripe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Comprehensive security tests for SecretString.
 * Ensures that sensitive data is properly protected from accidental exposure.
 */
class SecretStringSecurityTest {

    @Test
    fun testToStringDoesNotRevealSecret() {
        val secret = SecretString.wrap("super_secret_key_12345")
        val toString = secret.toString()

        // Verify the secret is not exposed in toString
        assertFalse(toString.contains("super_secret"), "toString should not contain secret content")
        assertFalse(toString.contains("12345"), "toString should not contain secret content")
        assertEquals("***REDACTED***", toString, "toString should return redacted placeholder")
    }

    @Test
    fun testToStringConsistency() {
        val secret1 = SecretString.wrap("my_secret_value")
        val secret2 = SecretString.wrap("different_secret")

        // All secrets should have the same toString output
        assertEquals(secret1.toString(), secret2.toString(), "All secrets should have identical toString output")
        assertEquals("***REDACTED***", secret1.toString())
        assertEquals("***REDACTED***", secret2.toString())
    }

    @Test
    fun testRevealReturnsActualValue() {
        val value = "my_secret_value"
        val secret = SecretString.wrap(value)

        assertEquals(value, secret.reveal(), "reveal() should return the actual secret value")
    }

    @Test
    fun testRevealWithSpecialCharacters() {
        val specialValues = listOf(
            "pk_test_51Abc!@#\$%^&*()_+-=[]{}|;:',.<>?",
            "secret\nwith\nnewlines",
            "secret\twith\ttabs",
            "secret with spaces and Unicode: 你好世界"
        )

        specialValues.forEach { value ->
            val secret = SecretString.wrap(value)
            assertEquals(value, secret.reveal(), "reveal() should handle special characters correctly")
            assertEquals("***REDACTED***", secret.toString(), "toString should still redact special characters")
        }
    }

    @Test
    fun testEqualityWorks() {
        val secret1 = SecretString.wrap("test_secret")
        val secret2 = SecretString.wrap("test_secret")
        val secret3 = SecretString.wrap("different_secret")

        assertEquals(secret1, secret2, "Identical secrets should be equal")
        assertNotEquals(secret1, secret3, "Different secrets should not be equal")
        assertNotEquals(secret2, secret3, "Different secrets should not be equal")
    }

    @Test
    fun testEqualityWithSameReference() {
        val secret = SecretString.wrap("test_secret")

        assertEquals(secret, secret, "Secret should equal itself")
        assertTrue(secret.equals(secret), "equals() should return true for same reference")
    }

    @Test
    fun testEqualityWithNull() {
        val secret = SecretString.wrap("test_secret")

        assertNotEquals<Any?>(secret, null, "Secret should not equal null")
        assertFalse(secret.equals(null), "equals(null) should return false")
    }

    @Test
    fun testEqualityWithDifferentType() {
        val secret = SecretString.wrap("test_secret")
        val plainString = "test_secret"

        assertNotEquals<Any>(secret, plainString, "SecretString should not equal plain String")
        assertFalse(secret.equals(plainString), "equals() should return false for different types")
    }

    @Test
    fun testHashCodeConsistency() {
        val secret1 = SecretString.wrap("test_secret")
        val secret2 = SecretString.wrap("test_secret")

        assertEquals(secret1.hashCode(), secret2.hashCode(), "Equal secrets should have equal hash codes")
    }

    @Test
    fun testHashCodeUniqueness() {
        val secret1 = SecretString.wrap("test_secret_1")
        val secret2 = SecretString.wrap("test_secret_2")

        // While hash codes can collide, different values should typically have different hashes
        // This is a probabilistic test - we're checking they're likely different
        assertNotEquals(secret1.hashCode(), secret2.hashCode(), "Different secrets should typically have different hash codes")
    }

    @Test
    fun testHashCodeStability() {
        val secret = SecretString.wrap("test_secret")
        val hash1 = secret.hashCode()
        val hash2 = secret.hashCode()

        assertEquals(hash1, hash2, "Hash code should be stable across multiple calls")
    }

    @Test
    fun testConstantTimeComparisonDifferentLengths() {
        val short = SecretString.wrap("abc")
        val long = SecretString.wrap("abcdef")

        assertNotEquals(short, long, "Secrets of different lengths should not be equal")
        assertNotEquals(long, short, "Order should not matter for inequality")
    }

    @Test
    fun testConstantTimeComparisonSimilarStrings() {
        // These strings differ only in one character
        val secret1 = SecretString.wrap("test_secret_a")
        val secret2 = SecretString.wrap("test_secret_b")
        val secret3 = SecretString.wrap("test_secret_a")

        assertNotEquals(secret1, secret2, "Similar but different secrets should not be equal")
        assertEquals(secret1, secret3, "Identical secrets should be equal")
    }

    @Test
    fun testWrapEmptyString() {
        val secret = SecretString.wrap("")
        assertEquals("", secret.reveal())
        assertEquals("***REDACTED***", secret.toString())
    }

    @Test
    fun testWrapVeryLongString() {
        val longValue = "a".repeat(10000)
        val secret = SecretString.wrap(longValue)

        assertEquals(longValue, secret.reveal())
        assertEquals("***REDACTED***", secret.toString())
        assertFalse(secret.toString().contains("a"), "toString should not contain any part of long secret")
    }

    @Test
    fun testSecretStringInCollections() {
        val secret1 = SecretString.wrap("secret1")
        val secret2 = SecretString.wrap("secret2")
        val secret3 = SecretString.wrap("secret1") // duplicate

        val set = setOf(secret1, secret2, secret3)

        // Set should contain only 2 unique secrets
        assertEquals(2, set.size, "Set should correctly handle SecretString equality")
        assertTrue(set.contains(secret1))
        assertTrue(set.contains(secret2))
        assertTrue(set.contains(secret3)) // Should be found via equality
    }

    @Test
    fun testSecretStringInMaps() {
        val secret1 = SecretString.wrap("key1")
        val secret2 = SecretString.wrap("key2")
        val secret3 = SecretString.wrap("key1") // duplicate

        val map = mutableMapOf<SecretString, String>()
        map[secret1] = "value1"
        map[secret2] = "value2"
        map[secret3] = "value3_updated" // Should update secret1's value

        assertEquals(2, map.size, "Map should correctly handle SecretString as keys")
        assertEquals("value3_updated", map[secret1])
        assertEquals("value3_updated", map[secret3])
        assertEquals("value2", map[secret2])
    }

    @Test
    fun testSecurityInheritanceProperties() {
        // Verify that SecretString properly overrides Object methods
        val secret = SecretString.wrap("test")

        // toString should be overridden
        val toStringResult = secret.toString()
        assertFalse(toStringResult.contains("test"))
        assertFalse(toStringResult.contains("SecretString@"))

        // equals should be overridden (not reference equality)
        val secret2 = SecretString.wrap("test")
        assertTrue(secret == secret2)
        assertTrue(secret !== secret2) // Different objects

        // hashCode should be overridden
        assertEquals(secret.hashCode(), secret2.hashCode())
    }

    @Test
    fun testNoLeakageInStringInterpolation() {
        val secret = SecretString.wrap("my_api_key")
        val message = "Using secret: $secret"

        assertFalse(message.contains("my_api_key"), "String interpolation should not reveal secret")
        assertTrue(message.contains("***REDACTED***"), "String interpolation should use toString()")
    }

    @Test
    fun testConstantTimeComparisonTimingResistance() {
        // This test verifies the constant-time comparison behavior
        // In a real timing attack, we'd need microsecond precision, but we can verify behavior

        val secret1 = SecretString.wrap("password123456")
        val secret2 = SecretString.wrap("password123457") // Differs at end
        val secret3 = SecretString.wrap("qassword123456") // Differs at start

        // Both should return false, regardless of where they differ
        assertFalse(secret1 == secret2, "Should not be equal (differs at end)")
        assertFalse(secret1 == secret3, "Should not be equal (differs at start)")

        // The constant-time comparison should prevent timing attacks
        // by always comparing the full length
    }

    @Test
    fun testWrapPreservesValue() {
        val testValues = listOf(
            "pk_test_51Abc123",
            "sk_live_XyZ987",
            "",
            "a",
            "🔐🔑",
            "line1\nline2",
            " spaces around "
        )

        testValues.forEach { value ->
            val secret = SecretString.wrap(value)
            assertEquals(value, secret.reveal(), "wrap should preserve exact value: $value")
        }
    }
}
