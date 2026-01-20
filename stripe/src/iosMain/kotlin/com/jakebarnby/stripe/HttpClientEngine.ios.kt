package com.jakebarnby.stripe

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

internal actual fun createHttpClientEngine(): HttpClientEngine = Darwin.create()
