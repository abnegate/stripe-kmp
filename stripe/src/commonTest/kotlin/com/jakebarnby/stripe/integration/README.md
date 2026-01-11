# Stripe KMP Integration Tests

This directory contains integration tests for the Stripe KMP wrapper library.

## Test Categories

### 1. Integration Tests (Require API Key)

Tests in `TokenIntegrationTest.kt` and `PaymentMethodIntegrationTest.kt` interact with the real Stripe API in test mode. They are automatically skipped if no API key is configured.

**To run with a real API key:**

```bash
# Set your test API key
export STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here

# Or call the configuration programmatically
TestConfiguration.setPublishableKey("pk_test_your_key_here")

# Run the tests
./gradlew :stripe:check
```

**Important:** These tests use Stripe's test mode and test card numbers, so they won't charge real money or affect production data.

### 2. Mock Tests (Always Run)

Tests in `MockStripeServerTest.kt` validate model behavior, validation logic, and data structures without making API calls. These tests always run and don't require an API key.

## Test Configuration

The `TestConfiguration` object provides:

- **Test card numbers** from Stripe's documentation:
  - `VISA_SUCCESS`: 4242424242424242 (succeeds)
  - `VISA_DECLINED`: 4000000000000002 (declines)
  - `VISA_REQUIRES_AUTH`: 4000002500003155 (requires 3D Secure)
  - `MASTERCARD`: 5555555555554444
  - `AMEX`: 378282246310005
  - `INVALID_LUHN`: 4242424242424241 (fails validation)

- **Test bank accounts**:
  - `ROUTING_NUMBER`: 110000000
  - `ACCOUNT_SUCCESS`: 000123456789 (succeeds)
  - `ACCOUNT_DECLINED`: 000111111116 (declines)

## Running Tests

### Run all tests
```bash
./gradlew :stripe:check
```

### Run specific platform tests
```bash
# iOS simulator
./gradlew :stripe:iosSimulatorArm64Test

# JavaScript/Browser
./gradlew :stripe:jsBrowserTest

# Android
./gradlew :stripe:testDebugUnitTest

# WASM
./gradlew :stripe:wasmJsBrowserTest
```

### Run only mock tests (no API key needed)
All tests will automatically skip integration tests if no API key is configured. The mock tests in `MockStripeServerTest.kt` will always run.

## What Gets Tested

### Token Creation
- ✅ Creating card tokens with various card types (Visa, Mastercard, Amex)
- ✅ Creating bank account tokens
- ✅ Card number validation (Luhn algorithm)
- ✅ Input sanitization (spaces, dashes)
- ✅ Error handling for invalid cards

### Payment Methods
- ✅ Creating payment methods with cards
- ✅ Adding billing details and addresses
- ✅ Retrieving payment methods
- ✅ Metadata support
- ✅ Multiple card brands

### Model Validation
- ✅ CardParams validation and sanitization
- ✅ BankAccountTokenParams validation
- ✅ BillingDetails email validation
- ✅ Address country code validation
- ✅ Enum conversions (PaymentIntentStatus, CardBrand, etc.)
- ✅ StripeResult operations (map, flatMap, etc.)
- ✅ IdempotencyKey generation and validation

## CI/CD Integration

These tests are designed to work in CI environments:

1. **Without API keys**: Mock tests run, integration tests skip gracefully
2. **With API keys**: Set `STRIPE_PUBLISHABLE_KEY` as a secret in your CI environment

Example GitHub Actions:
```yaml
- name: Run tests
  env:
    STRIPE_PUBLISHABLE_KEY: ${{ secrets.STRIPE_TEST_PUBLISHABLE_KEY }}
  run: ./gradlew :stripe:check
```

## Test Coverage

The integration tests provide real-world validation of:
- API request/response serialization
- Network error handling
- Platform-specific SDK behavior
- Idempotency key support
- Token and PaymentMethod lifecycle

The mock tests provide fast, reliable validation of:
- Business logic
- Data validation rules
- Builder patterns
- Type safety
- Error messages

## Adding New Tests

### For API-based tests:
1. Extend `IntegrationTestBase`
2. Call `skipIfNoApiKey()` at the start of each test
3. Use test data from `TestConfiguration`

```kotlin
class MyIntegrationTest : IntegrationTestBase() {
    @Test
    fun testNewFeature() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // Your test code here
    }
}
```

### For validation tests:
Add tests to `MockStripeServerTest.kt` - no special setup needed:

```kotlin
@Test
fun testModelValidation() {
    val model = MyModel(/* params */)
    assertEquals(expected, model.value)
}
```

## Resources

- [Stripe Test Cards](https://stripe.com/docs/testing#cards)
- [Stripe Test Bank Accounts](https://stripe.com/docs/testing#bank-accounts)
- [Stripe API Reference](https://stripe.com/docs/api)
