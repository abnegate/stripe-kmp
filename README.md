# stripe-kmp

A Kotlin Multiplatform library providing unified Stripe SDK bindings for Android, iOS, Web (JS), and WebAssembly (Wasm).

## Overview

This library enables you to integrate Stripe payments across all major platforms using a single, type-safe Kotlin API. It wraps the native Stripe SDKs for each platform:

- **JVM/Android**: Uses Stripe Java/Android SDK
- **iOS**: Uses Stripe iOS SDK via Kotlin/Native interop
- **Web (JS)**: Uses Stripe.js
- **WebAssembly**: Uses Stripe.js via Wasm-JS interop

## Features

- ✅ Kotlin 2.3.0 with the latest multiplatform features
- ✅ Support for Android, iOS, Web, and WebAssembly
- ✅ Type-safe API with sealed classes for result handling
- ✅ Coroutines support for asynchronous operations
- ✅ Common interface with platform-specific implementations

## Supported Platforms

| Platform | Status | Implementation |
|----------|--------|----------------|
| JVM/Android | ✅ | Stripe Java SDK |
| iOS (arm64, x64, simulatorArm64) | ✅ | Stripe iOS SDK |
| JavaScript (Browser) | ✅ | Stripe.js |
| WebAssembly | ✅ | Stripe.js |

## Installation

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.abnegate.stripe:stripe-kmp:1.0.0")
}
```

### Maven

```xml
<dependency>
    <groupId>com.abnegate.stripe</groupId>
    <artifactId>stripe-kmp</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Initialize the SDK

```kotlin
import com.abnegate.stripe.kmp.*

val stripe = createStripeSDK()
stripe.initialize(
    StripeConfiguration(
        publishableKey = "pk_test_your_key_here"
    )
)
```

### Create a Payment Method

```kotlin
val result = stripe.createPaymentMethod(
    cardNumber = "4242424242424242",
    expiryMonth = 12,
    expiryYear = 2025,
    cvc = "123"
)

when (result) {
    is StripeResult.Success -> {
        println("Payment method created: ${result.data.id}")
        println("Card last 4: ${result.data.card?.last4}")
    }
    is StripeResult.Error -> {
        println("Error: ${result.message}")
    }
}
```

### Confirm a Payment

```kotlin
val result = stripe.confirmPayment(
    clientSecret = "pi_xxx_secret_yyy",
    paymentMethodId = "pm_xxx"
)

when (result) {
    is StripeResult.Success -> {
        println("Payment confirmed: ${result.data.status}")
    }
    is StripeResult.Error -> {
        println("Payment failed: ${result.message}")
    }
}
```

### Retrieve a Payment Intent

```kotlin
val result = stripe.retrievePaymentIntent(
    clientSecret = "pi_xxx_secret_yyy"
)

when (result) {
    is StripeResult.Success -> {
        println("Payment intent: ${result.data.status}")
        println("Amount: ${result.data.amount} ${result.data.currency}")
    }
    is StripeResult.Error -> {
        println("Error: ${result.message}")
    }
}
```

## API Reference

### Core Types

#### `StripeSDK`

The main interface for interacting with Stripe:

```kotlin
interface StripeSDK {
    fun initialize(configuration: StripeConfiguration)
    suspend fun createPaymentMethod(cardNumber: String, expiryMonth: Int, expiryYear: Int, cvc: String): StripeResult<PaymentMethod>
    suspend fun confirmPayment(clientSecret: String, paymentMethodId: String): StripeResult<PaymentIntent>
    suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent>
}
```

#### `StripeConfiguration`

Configuration for initializing the SDK:

```kotlin
data class StripeConfiguration(
    val publishableKey: String,
    val merchantIdentifier: String? = null
)
```

#### `StripeResult<T>`

A sealed class for handling success and error states:

```kotlin
sealed class StripeResult<out T> {
    data class Success<T>(val data: T) : StripeResult<T>()
    data class Error(val message: String, val code: String? = null) : StripeResult<Nothing>()
}
```

## Building from Source

### Prerequisites

- JDK 17 or later
- Gradle 9.2.1 (included via wrapper)
- For iOS targets: macOS with Xcode

### Build Commands

```bash
# Build all targets
./gradlew build

# Build specific target
./gradlew jvmJar
./gradlew iosArm64Binaries
./gradlew jsBrowserProductionLibrary
./gradlew wasmJsBrowserProductionLibrary

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Platform-Specific Notes

### Android/JVM

The JVM implementation provides a simplified wrapper that can be extended to use the full Stripe Java/Android SDK. For production use, you should add the Stripe Android SDK dependency and implement the actual API calls.

### iOS

The iOS implementation uses Kotlin/Native to interface with the native Stripe iOS SDK. You'll need to add the Stripe iOS SDK as a CocoaPod or Swift Package dependency in your iOS project.

### Web (JS)

For web applications, include Stripe.js in your HTML:

```html
<script src="https://js.stripe.com/v3/"></script>
```

### WebAssembly

WebAssembly support uses the same Stripe.js library through Wasm-JS interop. Ensure Stripe.js is loaded in your host HTML page.

## Architecture

This project follows the Kotlin Multiplatform architecture pattern:

```
stripe-kmp/
├── commonMain/      # Shared interfaces and data models
├── jvmMain/         # JVM/Android implementation
├── iosMain/         # iOS implementation (shared across iOS targets)
├── jsMain/          # JavaScript/Browser implementation
└── wasmJsMain/      # WebAssembly implementation
```

## Version Information

- **Kotlin**: 2.3.0
- **Gradle**: 9.2.1
- **Coroutines**: 1.10.1
- **Target Platforms**: JVM 17, iOS 14+, Modern Browsers, Wasm

## Implementation Notes

This is a **simplified reference implementation** that demonstrates the architecture for a Kotlin Multiplatform Stripe SDK. In production, you should:

1. Implement actual calls to native Stripe SDKs instead of mock responses
2. Add proper error handling and validation
3. Include additional Stripe API features as needed
4. Add comprehensive tests for each platform
5. Implement proper security measures for handling payment data

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is provided as-is for demonstration purposes.

## Disclaimer

This is a demonstration project showing how to structure a Kotlin Multiplatform SDK for Stripe. It uses mock implementations and should not be used in production without proper implementation of actual Stripe SDK calls.

For production use, refer to:
- [Stripe Android SDK](https://github.com/stripe/stripe-android)
- [Stripe iOS SDK](https://github.com/stripe/stripe-ios)
- [Stripe.js Documentation](https://stripe.com/docs/js)

## Support

For issues and questions:
- File an issue on [GitHub](https://github.com/abnegate/stripe-kmp/issues)
- Check the [Stripe Documentation](https://stripe.com/docs)

