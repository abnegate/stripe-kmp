package com.jakebarnby.stripe

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

internal actual fun createHttpClientEngine(): HttpClientEngine = Js.create()
