# Stripe KMP

[![CI](https://github.com/jakebarnby/stripe-kmp/actions/workflows/ci.yml/badge.svg)](https://github.com/jakebarnby/stripe-kmp/actions/workflows/ci.yml)
[![Release](https://github.com/jakebarnby/stripe-kmp/actions/workflows/release.yml/badge.svg)](https://github.com/jakebarnby/stripe-kmp/actions/workflows/release.yml)

Kotlin Multiplatform wrapper for Stripe SDK providing a unified API across Android, iOS, Web (JS), and Server (JVM).

## Installation

### Gradle (Kotlin DSL)

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.jakebarnby.stripe:stripe-kmp:1.0.0")
}
```

### Platform-Specific Requirements

#### Android

No additional setup required. The Stripe Android SDK is included as a transitive dependency.

#### iOS

This library is intended to be consumed from Kotlin Multiplatform via Gradle.
It does not provide a standalone SPM or CocoaPods distribution. iOS frameworks
are produced as part of your KMP app build, and the Stripe iOS SDK dependencies
are wired via the library's CocoaPods configuration.

#### Web (JS)

No additional setup required. Stripe.js is loaded automatically from CDN.

#### Web (WASM)

Headless REST operations use Ktor CIO. Stripe.js is not used on WASM and UI flows
(PaymentSheet, 3DS, Apple Pay, Google Pay) are not supported. Browser-hosted WASM
is subject to CORS, so use the JS target for production web apps.

#### Server (JVM)

No additional setup required. The Stripe Java SDK is included as a transitive dependency.

## SDK Versions

| Platform | SDK | Version |
|----------|-----|---------|
| Android | com.stripe:stripe-android | 22.6.0 |
| Android | com.stripe:financial-connections | 22.6.0 |
| Android | com.stripe:identity | 22.6.0 |
| iOS | StripePaymentSheet (CocoaPods) | 25.5.0 |
| iOS | StripeFinancialConnections (CocoaPods) | 25.5.0 |
| Web (JS) | @stripe/stripe-js (npm) | 5.5.0 |
| Web (WASM) | Ktor CIO engine | 3.1.1 |
| Server | com.stripe:stripe-java | 28.2.0 |

## Platform Support

| Feature | Android | iOS | Web (JS) | WASM | Server (JVM) |
|---------|:-------:|:---:|:--------:|:----:|:------------:|
| Payment Methods | ✓ | ✓ | ✓ | ✓ | ✓ |
| Payment Intents | ✓ | ✓ | ✓ | ✓ | ✓ |
| Setup Intents | ✓ | ✓ | ✓ | ✓ | ✓ |
| Sources | ✓ | ✓ | ✓ | ✓ | ✓ |
| Tokens | ✓ | ✓ | ✓ | ✓ | ✓ |
| 3D Secure | ✓ | ✓ | ✓ | - | ✓ |
| Payment Sheet | ✓ | ✓ | ✓ | - | - |
| Apple Pay | - | ✓ | ✓ | - | - |
| Google Pay | ✓ | - | ✓ | - | - |
| Customer Retrieval | - | - | - | - | ✓ |
| Ephemeral Keys | - | - | - | - | ✓ |

> **Note**: WASM support is experimental. Headless REST operations require a host that can reach the Stripe API; browser builds may be blocked by CORS. Use the JS target for production web applications.

## Quick Start

### Client-Side (Android/iOS/Web)

```kotlin
import com.jakebarnby.stripe.Stripe
import com.jakebarnby.stripe.model.*

// Initialize Stripe with your publishable key
val stripe = Stripe.initialize(
    StripeConfiguration(
        publishableKey = "pk_test_...",
        enableLogging = true
    )
)

// Create a payment method
val paymentMethod = stripe.createPaymentMethod(
    PaymentMethodCreateParams.createCard(
        number = "4242424242424242",
        expMonth = 12,
        expYear = 2025,
        cvc = "123",
        billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com"
        )
    )
).getOrThrow()

// Confirm a payment intent
val paymentIntent = stripe.confirmPaymentIntent(
    ConfirmPaymentIntentParams.createWithPaymentMethodId(
        paymentMethodId = paymentMethod.id,
        clientSecret = "pi_xxx_secret_xxx"
    )
).getOrThrow()

// Handle 3D Secure if required
if (paymentIntent.status == PaymentIntentStatus.REQUIRES_ACTION) {
    val authenticatedIntent = stripe.handleNextActionForPayment(
        paymentIntent.clientSecret
    ).getOrThrow()
}
```

### Server-Side (JVM Only)

The JVM target includes additional server-only methods that are **not available** on client platforms (Android, iOS, Web). These methods require a secret API key and should only be used on your backend.

```kotlin
import com.jakebarnby.stripe.Stripe
import com.jakebarnby.stripe.model.*

// Initialize with your secret key (server-side only!)
val stripe = Stripe.initialize(
    StripeConfiguration(
        publishableKey = "sk_test_...", // Use secret key on server
        enableLogging = true
    )
)

// Server-only: Retrieve a customer
val customer = stripe.retrieveCustomer("cus_xxx").getOrThrow()
println("Customer: ${customer.name} (${customer.email})")

// Server-only: Create an ephemeral key for mobile SDK
val ephemeralKey = stripe.createEphemeralKey(
    EphemeralKeyCreateParams(
        customerId = "cus_xxx",
        stripeVersion = "2023-10-16"
    )
).getOrThrow()
```

> **Important**: `retrieveCustomer()` and `createEphemeralKey()` are only available on the JVM target. Attempting to use these methods from common code will result in a compile-time error, ensuring you don't accidentally expose secret key operations to client apps.

## API Reference

### Common Methods (All Platforms)

#### Token Creation
- `createCardToken(params: CardParams)` - Tokenize card data
- `createBankAccountToken(params: BankAccountTokenParams)` - Tokenize bank account
- `createPiiToken(params: PiiTokenParams)` - Tokenize PII data
- `createAccountToken(params: AccountParams)` - Create account token

#### Source Management
- `createSource(params: SourceParams)` - Create a payment source
- `retrieveSource(sourceId: String, clientSecret: String)` - Retrieve a source

#### Payment Methods
- `createPaymentMethod(params: PaymentMethodCreateParams)` - Create a payment method
- `retrievePaymentMethod(paymentMethodId: String)` - Retrieve a payment method

#### Payment Intents
- `retrievePaymentIntent(clientSecret: String)` - Retrieve a payment intent
- `confirmPaymentIntent(params: ConfirmPaymentIntentParams)` - Confirm a payment
- `handleNextActionForPayment(clientSecret: String)` - Handle 3DS authentication

#### Setup Intents
- `retrieveSetupIntent(clientSecret: String)` - Retrieve a setup intent
- `confirmSetupIntent(params: ConfirmSetupIntentParams)` - Confirm a setup intent
- `handleNextActionForSetupIntent(clientSecret: String)` - Handle 3DS for setup

### Server-Only Methods (JVM)

These methods are **only available on the JVM target** and will not compile on other platforms:

- `retrieveCustomer(customerId: String)` - Retrieve customer details
- `createEphemeralKey(params: EphemeralKeyCreateParams)` - Create ephemeral key for mobile SDK

## Error Handling

All methods return `StripeResult<T>` which provides safe error handling:

```kotlin
val result = stripe.createPaymentMethod(params)

// Functional style
result.onSuccess { paymentMethod ->
    println("Success: ${paymentMethod.id}")
}.onFailure { error ->
    println("Error: ${error.message}")
}

// Or get value directly (throws on failure)
val paymentMethod = result.getOrThrow()

// Or get nullable
val paymentMethodOrNull = result.getOrNull()
```

## Web-Specific Notes

### Stripe.js Loading

For web targets, Stripe.js loads asynchronously from CDN. Use `initializeAndAwait()` to ensure it's ready:

```kotlin
val stripe = Stripe.initializeAndAwait(
    StripeConfiguration(publishableKey = "pk_test_...")
)
```

### Idempotency Keys

Idempotency keys are not supported in client-side Stripe.js operations and will be ignored with a warning.

## Architecture

The library uses Kotlin Multiplatform's `expect`/`actual` mechanism:

```
commonMain/
├── Stripe.kt          # expect class with common API
├── model/             # Shared data models
└── ...

androidMain/
└── Stripe.android.kt  # actual implementation using Stripe Android SDK

iosMain/
└── Stripe.ios.kt      # actual implementation using Swift bridge

jsMain/
└── Stripe.js.kt       # actual implementation using Stripe.js

jvmMain/
└── Stripe.jvm.kt      # actual implementation using Stripe Java SDK
                       # + server-only methods
```

## Security

- **Never expose secret keys** in client applications
- Server-only methods (`retrieveCustomer`, `createEphemeralKey`) are compile-time restricted to JVM
- Use publishable keys (`pk_*`) on client platforms
- Use secret keys (`sk_*`) only on server (JVM target)
- The library handles PCI compliance by tokenizing card data before transmission

## License

MIT License - see [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.
