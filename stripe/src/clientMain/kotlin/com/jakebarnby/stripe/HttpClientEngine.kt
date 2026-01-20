package com.jakebarnby.stripe

import io.ktor.client.engine.*

/**
 * Platform-specific HttpClientEngine factory.
 *
 * Each platform provides its own implementation:
 * - iOS: Darwin engine
 * - Android: OkHttp engine
 * - JS: Js engine
 */
internal expect fun createHttpClientEngine(): HttpClientEngine
