package com.jakebarnby.stripe

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

internal actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()
