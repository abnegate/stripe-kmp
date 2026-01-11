package com.jakebarnby.stripe

/**
 * Idempotency key for safe API request retries.
 *
 * Stripe API requests can include an idempotency key to ensure that
 * the same operation isn't performed twice if a request is retried.
 *
 * Idempotency keys are sent in the `Idempotency-Key` header and must be unique
 * per request. They can be any string up to 255 characters.
 *
 * Example usage:
 * ```kotlin
 * // Generate a new random key
 * val key = IdempotencyKey.generate()
 *
 * // Create a payment with idempotency
 * val result = stripe.confirmPaymentIntent(params, idempotencyKey = key)
 *
 * // Use your own key value
 * val customKey = IdempotencyKey.fromValue("my-unique-request-id")
 * ```
 *
 * @property value The string value of the idempotency key
 */
public class IdempotencyKey private constructor(
    public val value: String
) {
    public companion object {
        private const val DEFAULT_KEY_LENGTH = 32
        private const val MAX_KEY_LENGTH = 255
        private const val KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        /**
         * Generate a new cryptographically secure random idempotency key.
         *
         * The generated key is a cryptographically secure random string of 32 alphanumeric characters.
         * Uses platform-specific secure random:
         * - Android/JVM: java.security.SecureRandom
         * - iOS: SecRandomCopyBytes
         * - JS/WASM: crypto.getRandomValues
         *
         * @return A new randomly generated idempotency key
         */
        public fun generate(): IdempotencyKey {
            val key = buildString(DEFAULT_KEY_LENGTH) {
                repeat(DEFAULT_KEY_LENGTH) {
                    append(KEY_CHARS[SecureRandom.nextInt(KEY_CHARS.length)])
                }
            }
            return IdempotencyKey(key)
        }

        /**
         * Create an idempotency key from an existing value.
         *
         * This is useful when you want to use your own unique identifier as the idempotency key,
         * such as a UUID, transaction ID, or other application-specific identifier.
         *
         * @param value The key value (must be non-blank and at most 255 characters)
         * @return An idempotency key with the specified value
         * @throws IllegalArgumentException if value is blank or exceeds 255 characters
         */
        public fun fromValue(value: String): IdempotencyKey {
            require(value.isNotBlank()) { "Idempotency key cannot be blank" }
            require(value.length <= MAX_KEY_LENGTH) {
                "Idempotency key must be at most $MAX_KEY_LENGTH characters, got ${value.length}"
            }
            return IdempotencyKey(value)
        }
    }

    override fun toString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as IdempotencyKey
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()
}
