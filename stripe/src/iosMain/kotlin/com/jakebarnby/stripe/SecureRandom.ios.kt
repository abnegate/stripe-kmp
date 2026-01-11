package com.jakebarnby.stripe

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

/**
 * iOS implementation using Security framework's SecRandomCopyBytes.
 */
internal actual object SecureRandom {
    @OptIn(ExperimentalForeignApi::class)
    actual fun nextBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
        }
        return bytes
    }

    actual fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        // Generate enough bytes to cover the bound
        val bytes = nextBytes(4)
        val value = ((bytes[0].toInt() and 0xFF) shl 24) or
                    ((bytes[1].toInt() and 0xFF) shl 16) or
                    ((bytes[2].toInt() and 0xFF) shl 8) or
                    (bytes[3].toInt() and 0xFF)
        // Use modulo with rejection sampling for unbiased distribution
        val unsignedValue = value.toLong() and 0xFFFFFFFFL
        return (unsignedValue % bound).toInt()
    }
}
