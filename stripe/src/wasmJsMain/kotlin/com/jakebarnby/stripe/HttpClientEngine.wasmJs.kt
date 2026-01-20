package com.jakebarnby.stripe

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

/**
 * WASM HttpClientEngine using CIO.
 */
internal actual fun createHttpClientEngine(): HttpClientEngine {
    return CIO.create()
}
