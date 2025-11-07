# Cashia Checkout Android SDK

A native Android library for integrating Cashia's hosted checkout into your Android applications using Jetpack Compose. Similar to Stripe's Payment Sheet and RevenueCat's Paywall, this SDK provides a pre-built, customizable checkout UI that can be easily embedded in your app.

## Features

✅ **Native Compose Integration** - Built entirely with Jetpack Compose for modern Android apps  
✅ **Pre-built UI** - No need to build your own checkout interface  
✅ **Secure HMAC Authentication** - Built-in authentication handling  
✅ **Easy Integration** - Just a few lines of code to get started  
✅ **Flexible Presentation** - Show as a composable or fullscreen dialog  
✅ **Comprehensive Error Handling** - Clear error states and callbacks  
✅ **Type-safe API** - Kotlin-first design with sealed classes for results

## Architecture

The SDK is split into two modules:

- **cashia-core**: Core functionality including API client, authentication, and data models
- **cashia-ui**: Compose UI components for displaying the checkout interface

## Installation

### Gradle

Add the Cashia Checkout SDK to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.cashia:cashia-core:1.0.0")
    implementation("com.cashia:cashia-ui:1.0.0")
}
```

Or if you're building from source, include the modules:

```kotlin
// settings.gradle.kts
include(":cashia-core")
include(":cashia-ui")

// build.gradle.kts
dependencies {
    implementation(project(":cashia-core"))
    implementation(project(":cashia-ui"))
}
```

## Quick Start

### 1. Initialize the SDK

Initialize the Cashia SDK in your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Cashia.initialize(
            context = this,
            configuration = CashiaConfiguration(
                keyId = "your_key_id",
                secretKey = "your_secret_key",
                environment = CashiaConfiguration.CashiaEnvironment.STAGING
            )
        )
    }
}
```

### 2. Present the Checkout

Use the `CashiaCheckoutDialog` composable to present the checkout UI:

```kotlin
@Composable
fun CheckoutScreen() {
    var showCheckout by remember { mutableStateOf(false) }
    
    Button(onClick = { showCheckout = true }) {
        Text("Checkout")
    }
    
    CashiaCheckoutDialog(
        showDialog = showCheckout,
        checkoutRequest = CheckoutSessionRequest(
            requestId = "order-123",
            currency = "USD",
            amount = 9000, // Amount in cents ($90.00)
            orderDetails = listOf(
                OrderDetail(
                    name = "Product Name",
                    currency = "USD",
                    quantity = 1,
                    description = "Product description",
                    price = 9000
                )
            )
        ),
        onResult = { result ->
            when (result) {
                is CheckoutResult.Success -> {
                    // Payment successful
                    println("Payment successful: ${result.requestId}")
                }
                is CheckoutResult.Failed -> {
                    // Payment failed
                    println("Payment failed: ${result.reason}")
                }
                is CheckoutResult.Cancelled -> {
                    // User cancelled
                    println("Checkout cancelled")
                }
                is CheckoutResult.Error -> {
                    // Error occurred
                    println("Error: ${result.message}")
                }
            }
            showCheckout = false
        }
    )
}
```

## Usage Examples

### Basic Checkout

```kotlin
val checkoutRequest = CheckoutSessionRequest(
    requestId = "order-${System.currentTimeMillis()}",
    currency = "USD",
    amount = 5000, // $50.00
    orderDetails = listOf(
        OrderDetail(
            name = "T-Shirt",
            currency = "USD",
            quantity = 2,
            description = "Cotton T-Shirt",
            price = 2500
        )
    )
)

CashiaCheckoutDialog(
    showDialog = showCheckout,
    checkoutRequest = checkoutRequest,
    onResult = { result -> /* Handle result */ }
)
```

### With Webhooks and Redirects

```kotlin
val checkoutRequest = CheckoutSessionRequest(
    requestId = "order-123",
    currency = "USD",
    amount = 10000,
    webhookUrl = "https://your-domain.com/webhook",
    successRedirectUrl = "https://your-domain.com/success",
    errorRedirectUrl = "https://your-domain.com/error",
    orderDetails = listOf(/* ... */),
    deliveryDetails = DeliveryDetails(
        currency = "USD",
        fee = 500 // $5.00 delivery fee
    )
)
```

### Inline Composable (Non-Dialog)

If you want more control over the presentation, use `CashiaCheckout` directly:

```kotlin
@Composable
fun CustomCheckoutScreen() {
    CashiaCheckout(
        checkoutRequest = checkoutRequest,
        onResult = { result ->
            // Handle result
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

## API Reference

### CashiaConfiguration

```kotlin
data class CashiaConfiguration(
    val keyId: String,           // Your API Key ID
    val secretKey: String,       // Your API Secret Key
    val environment: CashiaEnvironment // STAGING or PRODUCTION
)
```

### CheckoutSessionRequest

```kotlin
data class CheckoutSessionRequest(
    val requestId: String,                    // Unique order identifier
    val currency: String,                     // Currency code (e.g., "USD")
    val amount: Int,                         // Amount in smallest currency unit (cents eg 900000)
    val webhookUrl: String? = null,          // Optional webhook URL
    val successRedirectUrl: String? = null,  // Optional success redirect URL
    val errorRedirectUrl: String? = null,    // Optional error redirect URL
    val orderDetails: List<OrderDetail>,     // List of items
    val deliveryDetails: DeliveryDetails? = null // Optional delivery info
)
```

### CheckoutResult

```kotlin
sealed class CheckoutResult {
    data class Success(
        val requestId: String,
        val status: String
    ) : CheckoutResult()
    
    data class Failed(
        val requestId: String?,
        val reason: String
    ) : CheckoutResult()
    
    object Cancelled : CheckoutResult()
    
    data class Error(
        val error: Throwable,
        val message: String
    ) : CheckoutResult()
}
```

## Comparison with Other SDKs

### Like Stripe Payment Sheet

Just like Stripe's Payment Sheet for Android, Cashia Checkout provides:
- A pre-built payment UI
- Compose-first integration
- Simple presentation API
- Automatic session management

```kotlin
// Stripe Payment Sheet
paymentSheet.presentWithPaymentIntent(clientSecret, configuration)

// Cashia Checkout
CashiaCheckoutDialog(showDialog = true, checkoutRequest = request, onResult = {})
```

### Like RevenueCat Paywalls

Similar to RevenueCat's paywall presentation:
- Composable-based UI
- Flexible presentation options (dialog or inline)
- Result callbacks for handling outcomes

```kotlin
// RevenueCat Paywall
PaywallDialog(
    PaywallDialogOptions.Builder()
        .setListener(listener)
        .build()
)

// Cashia Checkout
CashiaCheckoutDialog(
    showDialog = true,
    checkoutRequest = request,
    onResult = { result -> }
)
```

## Advanced Usage

### Custom Error Handling

```kotlin
CashiaCheckoutDialog(
    showDialog = showCheckout,
    checkoutRequest = checkoutRequest,
    onResult = { result ->
        when (result) {
            is CheckoutResult.Success -> {
                // Update UI, save order locally
                updateOrderStatus(result.requestId, "completed")
                showSuccessScreen()
            }
            is CheckoutResult.Failed -> {
                // Log failure, show retry option
                logPaymentFailure(result.requestId, result.reason)
                showRetryDialog()
            }
            is CheckoutResult.Cancelled -> {
                // Analytics event
                trackCheckoutCancellation()
            }
            is CheckoutResult.Error -> {
                // Report to error tracking service
                reportError(result.error)
                showErrorMessage(result.message)
            }
        }
        showCheckout = false
    }
)
```

### Testing

For testing, use the staging environment:

```kotlin
Cashia.initialize(
    context = this,
    configuration = CashiaConfiguration(
        keyId = "test_key_id",
        secretKey = "test_secret_key",
        environment = CashiaConfiguration.CashiaEnvironment.STAGING
    )
)
```

## Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.20+
- **Jetpack Compose**: BOM 2023.10.01+

## Permissions

The SDK requires the following permission (automatically included):

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Security

- All API requests are authenticated using HMAC SHA256
- Use Android Keystore to manage your keys in production apps
- Use environment variables or secure storage for credentials
- The checkout UI runs in a secure WebView with JavaScript enabled

## Troubleshooting

### SDK Not Initialized Error

```
java.lang.IllegalStateException: Cashia SDK not initialized
```

**Solution**: Make sure you call `Cashia.initialize()` in your `Application.onCreate()` before using any SDK features.

### WebView Not Loading

If the checkout WebView is blank:
1. Check your internet connection
2. Verify your API credentials are correct
3. Ensure you're using the correct environment (staging vs production)
4. Check logcat for network errors

### HMAC Authentication Errors

If you're getting 401/403 errors:
1. Verify your `keyId` and `secretKey` are correct
2. Ensure system time is synchronized
3. Check that you're using the matching environment

## Sample App

A complete sample app is included in the `sample` module. To run it:

1. Open the project in Android Studio
2. Update the credentials in `SampleApplication.kt`
3. Run the app on a device or emulator

## License

MIT

## Support

For issues, questions, or feature requests:
- Email: support@cashia.com
- Documentation: https://developer.cashia.com
- GitHub Issues: 

## Changelog

### Version 1.0.0
- Initial release
- Compose-based checkout UI
- HMAC authentication
- Support for multiple payment methods
- Comprehensive error handling