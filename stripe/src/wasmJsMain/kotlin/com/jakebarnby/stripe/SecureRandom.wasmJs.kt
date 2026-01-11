package com.jakebarnby.stripe

/**
 * WASM JavaScript implementation using Web Crypto API.
 *
 * Note: WASM has different JS interop compared to regular JS target.
 * For security-critical operations, we use a simple fallback approach.
 */
internal actual object SecureRandom {
    actual fun nextBytes(size: Int): ByteArray {
        // In WASM, use the JS crypto API through a different mechanism
        val bytes = ByteArray(size)
        fillRandomBytes(bytes)
        return bytes
    }

    actual fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        val bytes = nextBytes(4)
        val value = ((bytes[0].toInt() and 0xFF) shl 24) or
                    ((bytes[1].toInt() and 0xFF) shl 16) or
                    ((bytes[2].toInt() and 0xFF) shl 8) or
                    (bytes[3].toInt() and 0xFF)
        val unsignedValue = value.toLong() and 0xFFFFFFFFL
        return (unsignedValue % bound).toInt()
    }
}

/**
 * Fill byte array with cryptographically secure random bytes using Web Crypto API.
 */
private fun fillRandomBytes(bytes: ByteArray) {
    // Use Kotlin's Random which falls back to secure implementations on WASM
    // This is a temporary solution until WASM JS interop matures
    for (i in bytes.indices) {
        bytes[i] = getSecureRandomByte()
    }
}

/**
 * Get a single secure random byte using JS crypto.
 */
private fun getSecureRandomByte(): Byte = js("crypto.getRandomValues(new Uint8Array(1))[0]")
