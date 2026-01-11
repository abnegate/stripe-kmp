package com.jakebarnby.stripe.sdk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform