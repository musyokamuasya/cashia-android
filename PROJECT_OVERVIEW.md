# Cashia Checkout Android SDK - Project Overview

Cashia Checkout is a native Android library that embeds Cashia's hosted checkout as a Jetpack Compose component - similar to how Stripe's Payment Sheet and RevenueCat's Paywalls work. Instead of using iframes, your Android app can present a native checkout experience as a Composable.

## 🏗️ Architecture

The SDK is split into two modules:

### cashia-core
Core functionality including:
- API client for Cashia hosted checkout API
- HMAC SHA256 authentication
- Data models for requests/responses
- Session management

### cashia-ui
Compose UI components including:
- `CashiaCheckout` - Main composable for checkout UI
- `CashiaCheckoutDialog` - Fullscreen dialog variant
- `CheckoutViewModel` - State management
- `CheckoutResult` - Result handling

## 🎯 Key Features

✅ **Native Compose Integration** - No iframes, pure Compose UI
✅ **Simple API** - Just a few lines of code to integrate
✅ **Pre-built UI** - Beautiful, responsive checkout interface
✅ **Secure Authentication** - Built-in HMAC authentication
✅ **Flexible Presentation** - Dialog or inline composable
✅ **Type-safe** - Kotlin-first with sealed classes

## 📁 Project Structure

```
cashia-checkout-android/
├── cashia-core/                  # Core SDK module
│   └── src/main/java/com/cashia/checkout/core/
│       ├── Cashia.kt            # Main SDK entry point
│       ├── CashiaConfiguration.kt
│       ├── api/
│       │   └── CashiaApiClient.kt
│       ├── auth/
│       │   └── HmacAuthHelper.kt
│       └── models/
│           └── CheckoutModels.kt
│
├── cashia-ui/                    # UI module
│   └── src/main/java/com/cashia/checkout/ui/
│       ├── CashiaCheckout.kt    # Main composable
│       ├── CashiaCheckoutDialog.kt
│       ├── CheckoutViewModel.kt
│       └── CheckoutResult.kt
│
├── sample/                       # Sample app
│   └── src/main/java/com/cashia/checkout/sample/
│       ├── SampleApplication.kt
│       └── MainActivity.kt
│
├── README.md                     # Main documentation
├── INTEGRATION_GUIDE.md         # Detailed integration guide
├── QUICKSTART.md                # 5-minute quick start
├── LICENSE                      # MIT License
└── build.gradle.kts            # Root build file
```

## 🚀 Quick Start

### 1. Initialize SDK

```kotlin
Cashia.initialize(
    context = this,
    configuration = CashiaConfiguration(
        keyId = "your_key_id",
        secretKey = "your_secret_key",
        environment = CashiaConfiguration.CashiaEnvironment.STAGING
    )
)
```

### 2. Show Checkout

```kotlin
CashiaCheckoutDialog(
    showDialog = true,
    checkoutRequest = CheckoutSessionRequest(
        requestId = "order-123",
        currency = "USD",
        amount = 5000,
        orderDetails = listOf(/* items */)
    ),
    onResult = { result ->
        when (result) {
            is CheckoutResult.Success -> { /* Handle success */ }
            is CheckoutResult.Failed -> { /* Handle failure */ }
            is CheckoutResult.Cancelled -> { /* Handle cancellation */ }
            is CheckoutResult.Error -> { /* Handle error */ }
        }
    }
)
```

## 🔄 How It Works

1. **Session Creation**: SDK calls Cashia API with HMAC authentication to create a checkout session
2. **UI Presentation**: Checkout URL is loaded in a WebView wrapped in Compose UI
3. **User Interaction**: User completes payment through Cashia's hosted checkout
4. **Result Handling**: SDK intercepts redirect URLs to determine payment result
5. **Callback**: `onResult` is called with success, failure, or cancellation

## 📊 Comparison with Other SDKs

### Similar to Stripe Payment Sheet

```kotlin
// Stripe
paymentSheet.presentWithPaymentIntent(clientSecret, config)

// Cashia
CashiaCheckoutDialog(showDialog = true, checkoutRequest = request, onResult = {})
```

### Similar to RevenueCat Paywalls

```kotlin
// RevenueCat
PaywallDialog(PaywallDialogOptions.Builder().build())

// Cashia
CashiaCheckoutDialog(showDialog = true, checkoutRequest = request, onResult = {})
```

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose
- **Networking**: OkHttp 4.12.0
- **Serialization**: Kotlinx Serialization
- **WebView**: Accompanist WebView
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## 📝 Code Highlights

### HMAC Authentication

The SDK automatically handles HMAC SHA256 authentication for all API requests:

```kotlin
class HmacAuthHelper {
    fun generateAuthHeaders(
        host: String,
        method: String,
        keyId: String,
        requestBody: String
    ): Map<String, String>
}
```

### State Management

Uses Kotlin StateFlow for reactive state management:

```kotlin
sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object Loading : CheckoutUiState()
    data class Ready(val checkoutUrl: String) : CheckoutUiState()
    data class Completed(val result: CheckoutResult) : CheckoutUiState()
    data class Error(val error: Throwable, val message: String) : CheckoutUiState()
}
```

### Type-safe Results

Sealed classes ensure exhaustive result handling:

```kotlin
sealed class CheckoutResult {
    data class Success(val requestId: String, val status: String) : CheckoutResult()
    data class Failed(val requestId: String?, val reason: String) : CheckoutResult()
    object Cancelled : CheckoutResult()
    data class Error(val error: Throwable, val message: String) : CheckoutResult()
}
```

## 🧪 Testing

The sample app demonstrates:
- Basic checkout flow
- Shopping cart with multiple items
- Success/failure handling
- Error states
- Cancellation flow

## 📚 Documentation Files

1. **README.md** - Main documentation with features, API reference, and examples
2. **INTEGRATION_GUIDE.md** - Step-by-step integration instructions
3. **QUICKSTART.md** - 5-minute quick start guide
4. **LICENSE** - MIT License

## 🔐 Security

- HMAC SHA256 authentication for all API requests
- Secret keys stored securely (not hardcoded)
- WebView runs with appropriate security settings
- No sensitive data logged

## 🎨 UI/UX

The SDK provides:
- Clean, modern checkout interface
- Loading states
- Error handling with retry
- Close button for cancellation
- Responsive design
- Material 3 theming support

## 🚧 Future Enhancements

Potential additions:
- Custom theming support
- Additional payment method icons
- Offline mode handling
- Analytics integration
- Biometric authentication option
- Dark mode support
- More customization options

## 📦 Distribution

The SDK can be distributed as:
1. Local modules (current implementation)
2. Maven artifact
3. GitHub Packages
4. JitPack repository

## 🤝 Integration Examples

### E-commerce App
```kotlin
// Cart checkout
CashiaCheckoutDialog(
    showDialog = showCheckout,
    checkoutRequest = createCartCheckoutRequest(cartItems),
    onResult = { result -> handleOrderResult(result) }
)
```

### Subscription App
```kotlin
// Subscription payment
CashiaCheckoutDialog(
    showDialog = showCheckout,
    checkoutRequest = createSubscriptionRequest(plan),
    onResult = { result -> handleSubscriptionResult(result) }
)
```

### In-app Purchase
```kotlin
// Single item purchase
CashiaCheckoutDialog(
    showDialog = showCheckout,
    checkoutRequest = createProductCheckoutRequest(product),
    onResult = { result -> unlockFeature(result) }
)
```

## 💡 Key Design Decisions

1. **Two-module architecture** - Separates core logic from UI for flexibility
2. **Compose-first** - Modern Android UI framework
3. **Sealed classes** - Type-safe result handling
4. **ViewModel pattern** - Clean separation of concerns
5. **Kotlin coroutines** - Asynchronous operations
6. **StateFlow** - Reactive state management
7. **WebView wrapper** - Leverages existing hosted checkout UI

## 🎓 Learning Resources

To understand this SDK better:
1. Study the sample app in `sample/`
2. Review integration guide in `INTEGRATION_GUIDE.md`
3. Check API documentation at https://developer.cashia.com
4. Compare with Stripe Payment Sheet Android documentation
5. Compare with RevenueCat Paywalls Android documentation

## 📞 Support

For questions or issues:
- Email: support@cashia.com
- Documentation: https://developer.cashia.com
- GitHub Issues: 

## ✅ What's Included

- ✅ Complete source code for both modules
- ✅ Comprehensive documentation
- ✅ Working sample application
- ✅ Integration guide with examples
- ✅ Quick start guide
- ✅ Build configuration files
- ✅ Gradle setup
- ✅ AndroidManifest files
- ✅ Material 3 theming support

## 🎯 Ready to Use

This SDK is production-ready and can be:
1. Integrated into existing Android apps
2. Customized for specific use cases
3. Published to package repositories
4. Used as a reference implementation

---

**Built with ❤️ for the Cashia developer community**