package com.jakebarnby.stripe

/**
 * Platform-specific cryptographically secure random number generator.
 *
 * This provides access to cryptographically secure random bytes for
 * security-sensitive operations like generating idempotency keys.
 *
 * Platform implementations:
 * - Android/JVM: Uses java.security.SecureRandom
 * - iOS: Uses SecRandomCopyBytes
 * - JS/WASM: Uses crypto.getRandomValues
 */
internal expect object SecureRandom {
    /**
     * Generate cryptographically secure random bytes.
     *
     * @param size Number of bytes to generate
     * @return ByteArray filled with random bytes
     */
    fun nextBytes(size: Int): ByteArray

    /**
     * Generate a cryptographically secure random integer in [0, bound).
     *
     * @param bound Upper bound (exclusive)
     * @return Random integer in [0, bound)
     */
    fun nextInt(bound: Int): Int
}
