package com.jakebarnby.stripe

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

internal actual fun createHttpClientEngine(): HttpClientEngine = CIO.create()
