# Example Usage

This document provides examples of using the Stripe KMP library in different contexts.

## Common Setup (All Platforms)

```kotlin
import com.abnegate.stripe.kmp.*

// Create and initialize the SDK
val stripe = createStripeSDK()
stripe.initialize(
    StripeConfiguration(
        publishableKey = "pk_test_YOUR_PUBLISHABLE_KEY"
    )
)
```

## Example 1: Create a Payment Method

```kotlin
suspend fun createPaymentMethodExample() {
    val stripe = createStripeSDK()
    stripe.initialize(
        StripeConfiguration(publishableKey = "pk_test_YOUR_KEY")
    )
    
    // Create a payment method from card details
    val result = stripe.createPaymentMethod(
        cardNumber = "4242424242424242",  // Test card
        expiryMonth = 12,
        expiryYear = 2025,
        cvc = "123"
    )
    
    when (result) {
        is StripeResult.Success -> {
            val paymentMethod = result.data
            println("✅ Payment method created!")
            println("   ID: ${paymentMethod.id}")
            println("   Type: ${paymentMethod.type}")
            paymentMethod.card?.let { card ->
                println("   Brand: ${card.brand}")
                println("   Last 4: ${card.last4}")
                println("   Expires: ${card.expiryMonth}/${card.expiryYear}")
            }
        }
        is StripeResult.Error -> {
            println("❌ Error creating payment method")
            println("   Message: ${result.message}")
            result.code?.let { println("   Code: $it") }
        }
    }
}
```

## Example 2: Confirm a Payment

```kotlin
suspend fun confirmPaymentExample(
    clientSecret: String,
    paymentMethodId: String
) {
    val stripe = createStripeSDK()
    stripe.initialize(
        StripeConfiguration(publishableKey = "pk_test_YOUR_KEY")
    )
    
    val result = stripe.confirmPayment(
        clientSecret = clientSecret,
        paymentMethodId = paymentMethodId
    )
    
    when (result) {
        is StripeResult.Success -> {
            val paymentIntent = result.data
            println("✅ Payment confirmed!")
            println("   ID: ${paymentIntent.id}")
            println("   Status: ${paymentIntent.status}")
            println("   Amount: ${paymentIntent.amount} ${paymentIntent.currency}")
        }
        is StripeResult.Error -> {
            println("❌ Payment confirmation failed")
            println("   Message: ${result.message}")
        }
    }
}
```

## Example 3: Complete Payment Flow

```kotlin
suspend fun completePaymentFlow(
    amount: Long,
    currency: String = "usd"
) {
    val stripe = createStripeSDK()
    stripe.initialize(
        StripeConfiguration(publishableKey = "pk_test_YOUR_KEY")
    )
    
    // Step 1: Create payment method
    println("Step 1: Creating payment method...")
    val paymentMethodResult = stripe.createPaymentMethod(
        cardNumber = "4242424242424242",
        expiryMonth = 12,
        expiryYear = 2025,
        cvc = "123"
    )
    
    when (paymentMethodResult) {
        is StripeResult.Success -> {
            val paymentMethodId = paymentMethodResult.data.id
            println("✅ Payment method created: $paymentMethodId")
            
            // Step 2: Create PaymentIntent on your server
            // (This is a placeholder - in reality, call your backend)
            val clientSecret = "pi_test_secret_from_your_server"
            
            // Step 3: Confirm the payment
            println("\nStep 2: Confirming payment...")
            val confirmResult = stripe.confirmPayment(
                clientSecret = clientSecret,
                paymentMethodId = paymentMethodId
            )
            
            when (confirmResult) {
                is StripeResult.Success -> {
                    println("✅ Payment successful!")
                    println("   Status: ${confirmResult.data.status}")
                    println("   Payment ID: ${confirmResult.data.id}")
                }
                is StripeResult.Error -> {
                    println("❌ Payment failed: ${confirmResult.message}")
                }
            }
        }
        is StripeResult.Error -> {
            println("❌ Failed to create payment method: ${paymentMethodResult.message}")
        }
    }
}
```

## Example 4: Retrieve Payment Intent

```kotlin
suspend fun checkPaymentStatus(clientSecret: String) {
    val stripe = createStripeSDK()
    stripe.initialize(
        StripeConfiguration(publishableKey = "pk_test_YOUR_KEY")
    )
    
    val result = stripe.retrievePaymentIntent(clientSecret)
    
    when (result) {
        is StripeResult.Success -> {
            val intent = result.data
            println("Payment Intent Status: ${intent.status}")
            println("Amount: ${intent.amount / 100.0} ${intent.currency.uppercase()}")
            
            when (intent.status) {
                "succeeded" -> println("✅ Payment completed successfully")
                "requires_payment_method" -> println("⏳ Waiting for payment method")
                "requires_confirmation" -> println("⏳ Requires confirmation")
                "requires_action" -> println("⚠️ Requires additional action")
                "processing" -> println("⏳ Payment is processing")
                "canceled" -> println("❌ Payment was canceled")
                else -> println("Status: ${intent.status}")
            }
        }
        is StripeResult.Error -> {
            println("❌ Failed to retrieve payment: ${result.message}")
        }
    }
}
```

## Platform-Specific Notes

### Android/JVM

```kotlin
// In your Android app
class PaymentActivity : AppCompatActivity() {
    private val stripe = createStripeSDK()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        stripe.initialize(
            StripeConfiguration(
                publishableKey = getString(R.string.stripe_publishable_key)
            )
        )
        
        lifecycleScope.launch {
            processPayment()
        }
    }
    
    private suspend fun processPayment() {
        // Use the stripe instance
    }
}
```

### iOS (Swift)

```swift
import StripeKMP

class PaymentViewController: UIViewController {
    let stripe = StripeSDKKt.createStripeSDK()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        stripe.initialize(configuration: StripeConfiguration(
            publishableKey: "pk_test_YOUR_KEY",
            merchantIdentifier: nil
        ))
    }
    
    func processPayment() async {
        let result = await stripe.createPaymentMethod(
            cardNumber: "4242424242424242",
            expiryMonth: 12,
            expiryYear: 2025,
            cvc: "123"
        )
        
        if let success = result as? StripeResultSuccess {
            print("Success: \(success.data)")
        } else if let error = result as? StripeResultError {
            print("Error: \(error.message)")
        }
    }
}
```

### Web (JavaScript)

```javascript
// Make sure Stripe.js is loaded
// <script src="https://js.stripe.com/v3/"></script>

import { createStripeSDK, StripeConfiguration } from 'stripe-kmp';

const stripe = createStripeSDK();
stripe.initialize(new StripeConfiguration('pk_test_YOUR_KEY'));

async function handlePayment() {
    const result = await stripe.createPaymentMethod(
        '4242424242424242',
        12,
        2025,
        '123'
    );
    
    if (result instanceof StripeResultSuccess) {
        console.log('Payment method created:', result.data);
    } else {
        console.error('Error:', result.message);
    }
}
```

### WebAssembly

```kotlin
// In your Compose for Web or Kotlin/Wasm app
fun main() {
    val stripe = createStripeSDK()
    stripe.initialize(
        StripeConfiguration(
            publishableKey = "pk_test_YOUR_KEY"
        )
    )
    
    MainScope().launch {
        val result = stripe.createPaymentMethod(
            cardNumber = "4242424242424242",
            expiryMonth = 12,
            expiryYear = 2025,
            cvc = "123"
        )
        
        console.log("Payment result:", result)
    }
}
```

## Error Handling Best Practices

```kotlin
suspend fun robustPaymentHandling() {
    val stripe = createStripeSDK()
    stripe.initialize(StripeConfiguration("pk_test_YOUR_KEY"))
    
    try {
        val result = stripe.createPaymentMethod(
            cardNumber = "4242424242424242",
            expiryMonth = 12,
            expiryYear = 2025,
            cvc = "123"
        )
        
        when (result) {
            is StripeResult.Success -> {
                // Success path
                handleSuccessfulPayment(result.data)
            }
            is StripeResult.Error -> {
                // Error path
                when (result.code) {
                    "card_declined" -> showCardDeclinedError()
                    "insufficient_funds" -> showInsufficientFundsError()
                    "network_error" -> showNetworkError()
                    else -> showGenericError(result.message)
                }
            }
        }
    } catch (e: Exception) {
        // Handle unexpected exceptions
        println("Unexpected error: ${e.message}")
        showGenericError("An unexpected error occurred")
    }
}

fun handleSuccessfulPayment(paymentMethod: PaymentMethod) {
    // Navigate to success screen or show confirmation
}

fun showCardDeclinedError() {
    // Show user-friendly error message
}

fun showInsufficientFundsError() {
    // Show user-friendly error message
}

fun showNetworkError() {
    // Show network error message with retry option
}

fun showGenericError(message: String) {
    // Show generic error message
}
```

## Test Cards

For testing, use these Stripe test card numbers:

| Card Number | Description |
|-------------|-------------|
| 4242424242424242 | Successful payment |
| 4000000000000002 | Card declined |
| 4000000000009995 | Insufficient funds |
| 4000002500003155 | Requires authentication |

For all test cards:
- Use any future expiry date
- Use any 3-digit CVC
- Use any valid US ZIP code

## Important Notes

1. **Never hardcode your publishable key** - use environment variables or build configuration
2. **Never expose your secret key** - only use it on your server
3. **Validate input** - always validate card details before submitting
4. **Handle errors gracefully** - provide clear feedback to users
5. **Test thoroughly** - use test mode before going live

## Resources

- [Stripe API Documentation](https://stripe.com/docs/api)
- [Stripe Testing Guide](https://stripe.com/docs/testing)
- [PCI Compliance](https://stripe.com/docs/security)
