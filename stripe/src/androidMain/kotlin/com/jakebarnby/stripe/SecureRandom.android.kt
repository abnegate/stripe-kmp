package com.jakebarnby.stripe

import java.security.SecureRandom as JavaSecureRandom

/**
 * Android/JVM implementation using java.security.SecureRandom.
 */
internal actual object SecureRandom {
    private val random = JavaSecureRandom()

    actual fun nextBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return bytes
    }

    actual fun nextInt(bound: Int): Int {
        return random.nextInt(bound)
    }
}
