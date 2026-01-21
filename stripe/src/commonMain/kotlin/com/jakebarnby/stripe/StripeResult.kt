package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.StripeException

/**
 * Result type for Stripe API operations.
 * Wraps successful results or errors in a type-safe way.
 */
public sealed class StripeResult<out T> {
    /**
     * Successful result containing a value.
     */
    public data class Success<T>(val value: T) : StripeResult<T>()

    /**
     * Failed result containing an error.
     */
    public data class Failure(val error: StripeException) : StripeResult<Nothing>()

    /**
     * Returns true if this result is a success.
     */
    public fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if this result is a failure.
     */
    public fun isFailure(): Boolean = this is Failure

    /**
     * Returns the value if this is a success, or null otherwise.
     */
    public fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    /**
     * Returns the error if this is a failure, or null otherwise.
     */
    public fun errorOrNull(): StripeException? = when (this) {
        is Success -> null
        is Failure -> error
    }

    /**
     * Returns the value if this is a success, or throws the error if this is a failure.
     */
    public fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    /**
     * Applies the given function if this is a success.
     */
    public inline fun <R> map(transform: (T) -> R): StripeResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> Failure(error)
    }

    /**
     * Applies the given function if this is a success, flattening the result.
     */
    public inline fun <R> flatMap(transform: (T) -> StripeResult<R>): StripeResult<R> = when (this) {
        is Success -> transform(value)
        is Failure -> Failure(error)
    }

    /**
     * Performs the given action if this is a success.
     */
    public inline fun onSuccess(action: (T) -> Unit): StripeResult<T> {
        if (this is Success) action(value)
        return this
    }

    /**
     * Performs the given action if this is a failure.
     */
    public inline fun onFailure(action: (StripeException) -> Unit): StripeResult<T> {
        if (this is Failure) action(error)
        return this
    }

    public companion object {
        /**
         * Creates a successful result.
         */
        public fun <T> success(value: T): StripeResult<T> = Success(value)

        /**
         * Creates a failed result.
         */
        public fun <T> failure(error: StripeException): StripeResult<T> = Failure(error)

        /**
         * Wraps a block that might throw an exception into a Result.
         */
        public inline fun <T> runCatching(block: () -> T): StripeResult<T> {
            return try {
                success(block())
            } catch (e: StripeException) {
                failure(e)
            } catch (e: Exception) {
                // Log the full exception details for debugging
                val errorMessage = buildString {
                    append(e.message ?: "Unknown error")
                    append(" [${e::class.simpleName}]")
                    e.cause?.let { cause ->
                        append(" - Caused by: ${cause.message} [${cause::class.simpleName}]")
                    }
                }
                failure(StripeException(errorMessage, cause = e))
            }
        }
    }
}
