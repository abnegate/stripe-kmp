package com.jakebarnby.stripe

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get

/**
 * External declaration for Web Crypto API.
 */
@JsName("crypto")
private external object JsCrypto {
    fun getRandomValues(array: Int8Array): Int8Array
}

/**
 * JavaScript implementation using Web Crypto API.
 */
internal actual object SecureRandom {
    actual fun nextBytes(size: Int): ByteArray {
        val array = Int8Array(size)
        JsCrypto.getRandomValues(array)
        return ByteArray(size) { array[it] }
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
