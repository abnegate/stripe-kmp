# Stripe KMP

[![CI](https://github.com/jakebarnby/stripe-kmp/actions/workflows/ci.yml/badge.svg)](https://github.com/jakebarnby/stripe-kmp/actions/workflows/ci.yml)
[![Release](https://github.com/jakebarnby/stripe-kmp/actions/workflows/release.yml/badge.svg)](https://github.com/jakebarnby/stripe-kmp/actions/workflows/release.yml)

Kotlin Multiplatform wrapper for Stripe SDK.

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM).

## Features

The Stripe KMP library provides a unified API for Stripe payments across all platforms:

- **Payment Methods**: Create and manage payment methods (cards, bank accounts, etc.)
- **Payment Intents**: Create and confirm payment intents with 3D Secure support
- **Setup Intents**: Set up payment methods for future use
- **Sources**: Create and manage payment sources (legacy API)
- **Tokens**: Tokenize sensitive payment data
- **Authentication**: Handle 3D Secure and other authentication flows

### Platform Support

| Feature | Android | iOS | Web (JS) | Desktop (JVM) |
|---------|---------|-----|----------|---------------|
| Payment Methods | ✓ | ✓ | ✓ | ✓ |
| Payment Intents | ✓ | ✓ | ✓ | ✓ |
| Setup Intents | ✓ | ✓ | ✓ | ✓ |
| Sources | ✓ | ✓ | ✓ | ✓ |
| Tokens | ✓ | ✓ | ✓ | ✓ |
| 3D Secure | ✓ | ✓ | ✓ | ✓ |
| Payment Sheet | ✓ | ✓ | ✓ | - |
| Apple Pay | - | ✓ | ✓ | - |
| Google Pay | ✓ | - | ✓ | - |

## JavaScript/Web Implementation

The JavaScript implementation uses Stripe.js for browser-based payments. All methods are production-ready and handle:

- Asynchronous loading of Stripe.js from CDN
- Promise-to-coroutine conversion for Kotlin/JS interop
- Proper error handling and validation
- Type-safe conversion between JavaScript objects and Kotlin data classes
- 3D Secure authentication flows

### Quick Start (Web)

```kotlin
import com.jakebarnby.stripe.Stripe
import com.jakebarnby.stripe.model.*

// Initialize Stripe
val stripe = Stripe.initializeAndAwait(
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

### Implemented JavaScript Methods

All methods use Stripe.js APIs under the hood:

1. **createPaymentMethod()** - Uses `stripe.createPaymentMethod()`
2. **createSource()** - Uses `stripe.createSource()`
3. **confirmPaymentIntent()** - Uses `stripe.confirmCardPayment()`
4. **confirmSetupIntent()** - Uses `stripe.confirmCardSetup()`
5. **retrievePaymentIntent()** - Uses `stripe.retrievePaymentIntent()`
6. **retrieveSetupIntent()** - Uses `stripe.retrieveSetupIntent()`
7. **handleNextActionForPayment()** - Uses `stripe.handleCardAction()`
8. **handleNextActionForSetupIntent()** - Uses `stripe.handleCardSetup()`

### Authentication Flow

The library automatically handles 3D Secure and other authentication challenges:

```kotlin
val authenticator = PaymentAuthenticator.getInstance()

// Authenticate a payment intent
val result = authenticator.authenticatePayment(
    activity = Unit, // Not used in JS
    clientSecret = "pi_xxx_secret_xxx"
)

when (result) {
    is AuthenticationResult.Completed -> {
        println("Payment succeeded: ${result.paymentIntent?.status}")
    }
    is AuthenticationResult.Failed -> {
        println("Authentication failed: ${result.error.message}")
    }
    is AuthenticationResult.Canceled -> {
        println("User canceled authentication")
    }
}
```

### Error Handling

All methods return `StripeResult<T>` which is either Success or Failure:

```kotlin
val result = stripe.createPaymentMethod(params)

result.onSuccess { paymentMethod ->
    println("Payment method created: ${paymentMethod.id}")
}.onFailure { error ->
    println("Error: ${error.message}")
}

// Or use getOrThrow() for exceptions
val paymentMethod = result.getOrThrow()
```

### Important Notes

- Stripe.js loads asynchronously from CDN. Use `initializeAndAwait()` to wait for it to load.
- Idempotency keys are not supported in client-side operations (ignored with warning).
- PCI compliance: Never log or store raw card numbers. Use tokens or payment methods.
- The library handles all JavaScript interop, null safety, and type conversions automatically.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./composeApp/src/commonMain/kotlin) is for code that's common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
      folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you're sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE's toolbar or build it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run Web Application

To build and run the development version of the web app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:

- for the Wasm target (faster, modern browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
      ```
- for the JS target (slower, supports older browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :composeApp:jsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeApp:jsBrowserDevelopmentRun
      ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE's toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).
