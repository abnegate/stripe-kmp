# iOS Stripe Bridge

This directory contains the Swift bridge implementation for integrating the Stripe iOS SDK with the Kotlin Multiplatform wrapper.

## Files

- **StripeBridge.swift** - Main Swift bridge implementation that wraps the native Stripe iOS SDK
- **StripeBridge.h** - Objective-C header for Swift bridge (for cinterop compatibility)

## Usage

### 1. Add to Your iOS Project

Copy both `StripeBridge.swift` and `StripeBridge.h` to your iOS app target in Xcode.

### 2. Install Stripe SDK

The Stripe iOS SDK is installed via CocoaPods. Run:

```bash
./gradlew podInstall
```

This will install:
- StripePaymentSheet (v24.5.0)
- StripeFinancialConnections (v24.5.0)

### 3. Initialize Stripe

In your iOS app (e.g., in `AppDelegate` or your root view):

```swift
import SwiftUI
import StripeKMP // The KMP framework

@main
struct YourApp: App {
    init() {
        // Initialize Stripe KMP wrapper
        let config = StripeConfiguration(
            publishableKey: "pk_test_...",
            enableLogging: true
        )
        Stripe.companion.initialize(configuration: config)

        // Initialize the native Stripe SDK through the bridge
        StripeBridge.shared.initialize(publishableKey: "pk_test_...")
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### 4. Set Presentation Context

Before calling any UI-related Stripe methods (like confirming payments or presenting PaymentSheet), you must set the presentation context:

```swift
struct CheckoutView: View {
    var body: some View {
        Button("Pay") {
            // Get the root view controller
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let rootVC = windowScene.windows.first?.rootViewController {
                // Set presentation context
                StripeBridge.shared.setPresentationContext(rootVC)

                // Now you can call Stripe methods from Kotlin
                // The bridge will handle presenting authentication UI
            }
        }
    }
}
```

## Bridge Implementation

The Swift bridge provides full implementations for:

### Token Creation
- `createCardToken()` - Create tokens from card details
- `createBankAccountToken()` - Create tokens from bank account details

### Payment Methods
- `createPaymentMethod()` - Create payment methods with card and billing details
- `retrievePaymentMethod()` - Retrieve existing payment method (returns error - server-side only)

### Payment Intents
- `retrievePaymentIntent()` - Retrieve a PaymentIntent by client secret
- `confirmPaymentIntent()` - Confirm and complete a payment
- `handleNextActionForPayment()` - Handle 3D Secure or other authentication

### Setup Intents
- `retrieveSetupIntent()` - Retrieve a SetupIntent by client secret
- `confirmSetupIntent()` - Confirm a setup intent for future payments
- `handleNextActionForSetupIntent()` - Handle authentication for setup

### Sources (Legacy)
- `createSource()` - Create payment sources
- `retrieveSource()` - Retrieve existing source

### PaymentSheet
- `presentPaymentSheet()` - Present Stripe's pre-built payment UI

## Example Usage from Kotlin

```kotlin
// In your Kotlin code
val stripe = Stripe.getInstance()

// Create a payment method
val result = stripe.createCardToken(
    CardParams(
        number = "4242424242424242",
        expMonth = 12,
        expYear = 2025,
        cvc = "123"
    )
)

when (result) {
    is StripeResult.Success -> {
        println("Token created: ${result.value.id}")
    }
    is StripeResult.Failure -> {
        println("Error: ${result.exception.message}")
    }
}
```

## Architecture

```
┌─────────────────────────────────────┐
│   Kotlin Multiplatform Code        │
│   (Your business logic)             │
└─────────────────┬───────────────────┘
                  │
                  ↓
┌─────────────────────────────────────┐
│   Stripe.ios.kt                     │
│   (Stub implementation with         │
│    instructions)                    │
└─────────────────┬───────────────────┘
                  │
                  ↓ (You implement this bridge)
┌─────────────────────────────────────┐
│   StripeBridge.swift                │
│   (Wraps native Stripe iOS SDK)    │
└─────────────────┬───────────────────┘
                  │
                  ↓
┌─────────────────────────────────────┐
│   Native Stripe iOS SDK             │
│   (via CocoaPods)                   │
└─────────────────────────────────────┘
```

## Implementation Notes

1. **Thread Safety**: The StripeBridge is a singleton and thread-safe
2. **Completion Handlers**: All async operations use Swift completion handlers that integrate with Kotlin coroutines
3. **Error Handling**: Errors from the Stripe SDK are passed through to Kotlin as StripeException
4. **Type Mapping**: Swift types are converted to Kotlin-compatible primitives (String, Int, etc.)

## Advanced Usage

### Custom Integration

If you need custom behavior, you can:

1. Modify `StripeBridge.swift` to add new methods
2. Update `Stripe.ios.kt` to call your new bridge methods
3. Keep the bridge interface consistent with the Kotlin API

### Testing

For testing without a real Stripe integration:

1. Keep the stub implementation in `Stripe.ios.kt`
2. Mock the responses in your test code
3. Or implement a test-only bridge that returns mock data

## Troubleshooting

### "StripeBridge not fully linked" error

This means the Kotlin code is using the stub implementation. Make sure you:
1. Copied `StripeBridge.swift` to your iOS project
2. The file is included in your iOS app target
3. You initialized the bridge: `StripeBridge.shared.initialize(publishableKey: "...")`

### "No presentation context set" error

Before calling any method that requires UI (confirm payment, handle next action):
```swift
StripeBridge.shared.setPresentationContext(yourViewController)
```

### CocoaPods errors

Run these commands:
```bash
./gradlew podInstall
cd iosApp
pod install
```

## License

The StripeBridge implementation is provided as reference code for integrating with Stripe's iOS SDK.
Stripe SDK usage is subject to Stripe's terms of service.
