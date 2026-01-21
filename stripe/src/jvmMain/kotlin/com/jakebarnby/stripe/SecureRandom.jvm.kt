package com.jakebarnby.stripe

/**
 * JVM implementation of SecureRandom using java.security.SecureRandom.
 */
internal actual object SecureRandom {
    private val random = java.security.SecureRandom()

    actual fun nextBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return bytes
    }

    actual fun nextInt(bound: Int): Int {
        return random.nextInt(bound)
    }
}
