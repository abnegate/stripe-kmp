package com.jakebarnby.stripe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IdempotencyKeyTest {

    @Test
    fun testGenerateCreatesNonEmptyKey() {
        val key = IdempotencyKey.generate()
        assertNotNull(key.value)
        assertTrue(key.value.isNotEmpty(), "Generated key should not be empty")
    }

    @Test
    fun testGenerateCreatesKeyWithCorrectLength() {
        val key = IdempotencyKey.generate()
        assertEquals(32, key.value.length, "Generated key should be 32 characters long")
    }

    @Test
    fun testGenerateCreatesAlphanumericKey() {
        val key = IdempotencyKey.generate()
        val alphanumericRegex = Regex("^[A-Za-z0-9]+$")
        assertTrue(
            alphanumericRegex.matches(key.value),
            "Generated key should only contain alphanumeric characters"
        )
    }

    @Test
    fun testGenerateCreatesUniqueKeys() {
        val keys = List(100) { IdempotencyKey.generate() }
        val uniqueKeys = keys.map { it.value }.toSet()
        assertEquals(
            keys.size,
            uniqueKeys.size,
            "All generated keys should be unique"
        )
    }

    @Test
    fun testFromValueWithValidString() {
        val value = "my-unique-request-id"
        val key = IdempotencyKey.fromValue(value)
        assertEquals(value, key.value, "Key value should match input")
    }

    @Test
    fun testFromValueWithMaxLength() {
        val value = "a".repeat(255)
        val key = IdempotencyKey.fromValue(value)
        assertEquals(value, key.value, "Key should accept 255 character string")
    }

    @Test
    fun testFromValueRejectsBlankString() {
        assertFailsWith<IllegalArgumentException>(
            message = "Should reject blank string"
        ) {
            IdempotencyKey.fromValue("")
        }
    }

    @Test
    fun testFromValueRejectsWhitespaceOnlyString() {
        assertFailsWith<IllegalArgumentException>(
            message = "Should reject whitespace-only string"
        ) {
            IdempotencyKey.fromValue("   ")
        }
    }

    @Test
    fun testFromValueRejectsTooLongString() {
        val value = "a".repeat(256)
        val exception = assertFailsWith<IllegalArgumentException>(
            message = "Should reject string longer than 255 characters"
        ) {
            IdempotencyKey.fromValue(value)
        }
        assertTrue(
            exception.message?.contains("255") == true,
            "Error message should mention the 255 character limit"
        )
    }

    @Test
    fun testToStringReturnsValue() {
        val value = "test-key-123"
        val key = IdempotencyKey.fromValue(value)
        assertEquals(value, key.toString(), "toString() should return the key value")
    }

    @Test
    fun testEqualsWithSameValue() {
        val value = "same-key"
        val key1 = IdempotencyKey.fromValue(value)
        val key2 = IdempotencyKey.fromValue(value)
        assertEquals(key1, key2, "Keys with same value should be equal")
    }

    @Test
    fun testEqualsWithDifferentValues() {
        val key1 = IdempotencyKey.fromValue("key-1")
        val key2 = IdempotencyKey.fromValue("key-2")
        assertNotEquals(key1, key2, "Keys with different values should not be equal")
    }

    @Test
    fun testEqualsWithSameInstance() {
        val key = IdempotencyKey.generate()
        assertEquals(key, key, "Key should equal itself")
    }

    @Test
    fun testHashCodeConsistency() {
        val value = "consistent-key"
        val key1 = IdempotencyKey.fromValue(value)
        val key2 = IdempotencyKey.fromValue(value)
        assertEquals(
            key1.hashCode(),
            key2.hashCode(),
            "Keys with same value should have same hash code"
        )
    }

    @Test
    fun testHashCodeDifference() {
        val key1 = IdempotencyKey.fromValue("key-1")
        val key2 = IdempotencyKey.fromValue("key-2")
        assertNotEquals(
            key1.hashCode(),
            key2.hashCode(),
            "Keys with different values should typically have different hash codes"
        )
    }

    @Test
    fun testFromValueWithSpecialCharacters() {
        val value = "key-with_special.chars@123"
        val key = IdempotencyKey.fromValue(value)
        assertEquals(value, key.value, "Key should accept special characters")
    }

    @Test
    fun testFromValueWithUUID() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val key = IdempotencyKey.fromValue(uuid)
        assertEquals(uuid, key.value, "Key should accept UUID format")
    }

    @Test
    fun testGeneratedKeyCanBeUsedWithFromValue() {
        val generatedKey = IdempotencyKey.generate()
        val recreatedKey = IdempotencyKey.fromValue(generatedKey.value)
        assertEquals(
            generatedKey,
            recreatedKey,
            "Key created from generated value should be equal"
        )
    }

    @Test
    fun testKeyValueImmutable() {
        val originalValue = "immutable-key"
        val key = IdempotencyKey.fromValue(originalValue)
        assertEquals(originalValue, key.value, "Key value should remain unchanged")
        // Attempting to recreate with same value should create equal key
        val sameKey = IdempotencyKey.fromValue(originalValue)
        assertEquals(key, sameKey, "Keys with same value should always be equal")
    }

    @Test
    fun testMultipleGenerationsAreRandom() {
        // Generate multiple keys and check they follow expected distribution
        val keys = List(1000) { IdempotencyKey.generate() }

        // All keys should be unique
        val uniqueKeys = keys.map { it.value }.toSet()
        assertTrue(
            uniqueKeys.size >= 999, // Allow for tiny chance of collision
            "Generated keys should be highly unique"
        )

        // Check that different characters are used (not all the same character)
        val allChars = keys.joinToString("") { it.value }.toSet()
        assertTrue(
            allChars.size > 10,
            "Generated keys should use variety of characters"
        )
    }

    @Test
    fun testFromValueErrorMessage() {
        val exception = assertFailsWith<IllegalArgumentException> {
            IdempotencyKey.fromValue("")
        }
        assertTrue(
            exception.message?.lowercase()?.contains("blank") == true,
            "Error message should mention blank key"
        )
    }

    @Test
    fun testFromValueLengthErrorMessage() {
        val longValue = "a".repeat(300)
        val exception = assertFailsWith<IllegalArgumentException> {
            IdempotencyKey.fromValue(longValue)
        }
        assertTrue(
            exception.message?.contains("255") == true,
            "Error message should mention max length"
        )
        assertTrue(
            exception.message?.contains("300") == true,
            "Error message should mention actual length"
        )
    }
}
