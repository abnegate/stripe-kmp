package com.jakebarnby.stripe

/**
 * A wrapper for sensitive strings like client secrets that prevents accidental logging or serialization.
 *
 * This class ensures that sensitive data is not exposed through toString() or accidental serialization.
 * Use the [reveal] method to access the actual value only when necessary.
 */
public class SecretString private constructor(private val value: String) {
    public companion object {
        /**
         * Create a SecretString from a regular string.
         *
         * @param value The sensitive string to wrap
         * @return A SecretString wrapper
         */
        public fun wrap(value: String): SecretString = SecretString(value)
    }

    /**
     * Reveal the actual secret value. Use with caution.
     *
     * WARNING: The revealed value should never be logged or exposed to external systems.
     *
     * @return The actual secret string
     */
    public fun reveal(): String = value

    /**
     * Returns a redacted representation of the secret.
     */
    override fun toString(): String = "***REDACTED***"

    /**
     * Constant-time equality comparison to prevent timing attacks.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SecretString) return false
        // Use constant-time comparison to prevent timing attacks
        return constantTimeEquals(value, other.value)
    }

    /**
     * Performs constant-time string comparison to prevent timing attacks.
     * This ensures that the comparison time is independent of where strings differ.
     *
     * Security properties:
     * - Always iterates over the maximum length of both strings
     * - Uses bitwise operations that don't short-circuit
     * - Converts to byte arrays to avoid Unicode normalization issues
     */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.encodeToByteArray()
        val bBytes = b.encodeToByteArray()

        // Length difference is captured in result but doesn't cause early exit
        var result = aBytes.size xor bBytes.size
        val maxLen = maxOf(aBytes.size, bBytes.size)

        // Always iterate over the longer string's length to prevent timing leakage
        for (i in 0 until maxLen) {
            val aByte = if (i < aBytes.size) aBytes[i].toInt() else 0
            val bByte = if (i < bBytes.size) bBytes[i].toInt() else 0
            result = result or (aByte xor bByte)
        }
        return result == 0
    }

    override fun hashCode(): Int = value.hashCode()
}
