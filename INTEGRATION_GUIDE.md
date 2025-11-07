# Cashia Checkout Android SDK - Integration Guide

This guide provides step-by-step instructions for integrating the Cashia Checkout SDK into your Android application.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [SDK Initialization](#sdk-initialization)
4. [Basic Integration](#basic-integration)
5. [Advanced Features](#advanced-features)
6. [Migration Guide](#migration-guide)
7. [Best Practices](#best-practices)

## Prerequisites

Before integrating the SDK, ensure you have:

- Android Studio Arctic Fox (2020.3.1) or later
- Minimum SDK 24 (Android 7.0)
- Kotlin 1.9.20 or later
- Jetpack Compose enabled in your project
- Cashia merchant account with API credentials

## Installation

### Step 1: Add Dependencies

Add the Cashia SDK modules to your app's `build.gradle.kts`:

```kotlin
dependencies {
    // Cashia Checkout SDK
    implementation(project(":cashia-core"))
    implementation(project(":cashia-ui"))
    
    // Required dependencies (if not already included)
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.1")
}
```

### Step 2: Enable Compose

Ensure Compose is enabled in your `build.gradle.kts`:

```kotlin
android {
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

### Step 3: Sync Project

Click "Sync Now" in Android Studio to download the dependencies.

## SDK Initialization

### Step 1: Create Application Class

If you don't already have one, create an `Application` class:

```kotlin
import android.app.Application
import com.cashia.checkout.core.Cashia
import com.cashia.checkout.core.CashiaConfiguration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeCashia()
    }
    
    private fun initializeCashia() {
        Cashia.initialize(
            context = this,
            configuration = CashiaConfiguration(
                keyId = BuildConfig.CASHIA_KEY_ID,
                secretKey = BuildConfig.CASHIA_SECRET_KEY,
                environment = if (BuildConfig.DEBUG) {
                    CashiaConfiguration.CashiaEnvironment.STAGING
                } else {
                    CashiaConfiguration.CashiaEnvironment.PRODUCTION
                }
            )
        )
    }
}
```

### Step 2: Register Application Class

Add your Application class to `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.YourApp">
    <!-- ... -->
</application>
```

### Step 3: Store Credentials Securely

**For Development:**

Add to `gradle.properties`:

```properties
CASHIA_KEY_ID=your_staging_key_id
CASHIA_SECRET_KEY=your_staging_secret_key
```

Access in `build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "CASHIA_KEY_ID", "\"${project.property("CASHIA_KEY_ID")}\"")
        buildConfigField("String", "CASHIA_SECRET_KEY", "\"${project.property("CASHIA_SECRET_KEY")}\"")
    }
}
```

**For Production:**

Use environment variables or a secure secrets management system. Never commit production credentials to version control.

## Basic Integration

### Example 1: Simple Checkout Button

```kotlin
@Composable
fun ProductScreen() {
    var showCheckout by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Your product UI
        Text("Product: Premium Subscription")
        Text("Price: $29.99")
        
        Button(onClick = { showCheckout = true }) {
            Text("Buy Now")
        }
    }
    
    // Cashia Checkout
    CashiaCheckoutDialog(
        showDialog = showCheckout,
        checkoutRequest = CheckoutSessionRequest(
            requestId = "sub-${System.currentTimeMillis()}",
            currency = "USD",
            amount = 2999,
            orderDetails = listOf(
                OrderDetail(
                    name = "Premium Subscription",
                    currency = "USD",
                    quantity = 1,
                    description = "1 month premium access",
                    price = 2999
                )
            )
        ),
        onResult = { result ->
            handleCheckoutResult(result)
            showCheckout = false
        }
    )
}

fun handleCheckoutResult(result: CheckoutResult) {
    when (result) {
        is CheckoutResult.Success -> {
            // Grant access to premium features
            unlockPremiumFeatures()
            showSuccessMessage()
        }
        is CheckoutResult.Failed -> {
            showErrorMessage("Payment failed: ${result.reason}")
        }
        is CheckoutResult.Cancelled -> {
            // User cancelled, maybe show a discount offer
        }
        is CheckoutResult.Error -> {
            showErrorMessage("Error: ${result.message}")
        }
    }
}
```

### Example 2: Shopping Cart Checkout

```kotlin
@Composable
fun ShoppingCartScreen(cartItems: List<CartItem>) {
    var showCheckout by remember { mutableStateOf(false) }
    
    val totalAmount = cartItems.sumOf { it.price * it.quantity }
    
    Column {
        // Cart items list
        LazyColumn {
            items(cartItems) { item ->
                CartItemRow(item)
            }
        }
        
        // Checkout button
        Button(
            onClick = { showCheckout = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Checkout - $${totalAmount / 100.0}")
        }
    }
    
    CashiaCheckoutDialog(
        showDialog = showCheckout,
        checkoutRequest = CheckoutSessionRequest(
            requestId = "cart-${System.currentTimeMillis()}",
            currency = "USD",
            amount = totalAmount,
            orderDetails = cartItems.map { item ->
                OrderDetail(
                    name = item.name,
                    currency = "USD",
                    quantity = item.quantity,
                    description = item.description,
                    price = item.price
                )
            },
            deliveryDetails = DeliveryDetails(
                currency = "USD",
                fee = 500 // $5.00 shipping
            )
        ),
        onResult = { result ->
            when (result) {
                is CheckoutResult.Success -> {
                    clearCart()
                    navigateToOrderConfirmation(result.requestId)
                }
                else -> handleCheckoutResult(result)
            }
            showCheckout = false
        }
    )
}
```

### Example 3: Subscription Payment

```kotlin
@Composable
fun SubscriptionScreen() {
    var showCheckout by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    
    Column {
        SubscriptionPlanCard(
            plan = SubscriptionPlan.MONTHLY,
            onClick = {
                selectedPlan = SubscriptionPlan.MONTHLY
                showCheckout = true
            }
        )
        
        SubscriptionPlanCard(
            plan = SubscriptionPlan.YEARLY,
            onClick = {
                selectedPlan = SubscriptionPlan.YEARLY
                showCheckout = true
            }
        )
    }
    
    selectedPlan?.let { plan ->
        CashiaCheckoutDialog(
            showDialog = showCheckout,
            checkoutRequest = CheckoutSessionRequest(
                requestId = "subscription-${System.currentTimeMillis()}",
                currency = "USD",
                amount = plan.price,
                webhookUrl = "https://yourapi.com/webhooks/cashia",
                successRedirectUrl = "yourapp://subscription/success",
                errorRedirectUrl = "yourapp://subscription/error",
                orderDetails = listOf(
                    OrderDetail(
                        name = plan.name,
                        currency = "USD",
                        quantity = 1,
                        description = plan.description,
                        price = plan.price
                    )
                )
            ),
            onResult = { result ->
                handleSubscriptionResult(result, plan)
                showCheckout = false
                selectedPlan = null
            }
        )
    }
}
```

## Advanced Features

### Custom Loading State

```kotlin
@Composable
fun CustomCheckoutFlow() {
    var checkoutState by remember { mutableStateOf<CheckoutState>(CheckoutState.Idle) }
    
    when (checkoutState) {
        CheckoutState.Idle -> {
            Button(onClick = { checkoutState = CheckoutState.ShowCheckout }) {
                Text("Checkout")
            }
        }
        CheckoutState.ShowCheckout -> {
            CashiaCheckoutDialog(
                showDialog = true,
                checkoutRequest = createCheckoutRequest(),
                onResult = { result ->
                    checkoutState = CheckoutState.Completed(result)
                }
            )
        }
        is CheckoutState.Completed -> {
            val result = (checkoutState as CheckoutState.Completed).result
            ResultScreen(result)
        }
    }
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object ShowCheckout : CheckoutState()
    data class Completed(val result: CheckoutResult) : CheckoutState()
}
```

### Webhook Integration

When using webhooks, implement a webhook endpoint on your server:

```kotlin
// Your server-side code (Kotlin + Ktor example)
post("/webhooks/cashia") {
    val payload = call.receive<WebhookPayload>()
    
    when (payload.event) {
        "payment.success" -> {
            // Verify webhook signature (implement HMAC verification)
            if (verifyWebhookSignature(payload)) {
                // Update database
                updateOrderStatus(payload.requestId, "paid")
                
                // Send confirmation email
                sendOrderConfirmationEmail(payload.requestId)
                
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
        "payment.failed" -> {
            updateOrderStatus(payload.requestId, "failed")
            call.respond(HttpStatusCode.OK)
        }
    }
}
```

### Analytics Integration

```kotlin
fun handleCheckoutResult(result: CheckoutResult) {
    when (result) {
        is CheckoutResult.Success -> {
            // Track successful purchase
            FirebaseAnalytics.getInstance(this).logEvent("purchase") {
                param("transaction_id", result.requestId)
                param("value", amount / 100.0)
                param("currency", "USD")
            }
        }
        is CheckoutResult.Failed -> {
            // Track failed payment
            FirebaseAnalytics.getInstance(this).logEvent("checkout_failed") {
                param("reason", result.reason)
            }
        }
        is CheckoutResult.Cancelled -> {
            // Track checkout abandonment
            FirebaseAnalytics.getInstance(this).logEvent("checkout_abandoned") {}
        }
    }
}
```

## Migration Guide

### From WebView Implementation

If you're currently using a WebView to show Cashia checkout:

**Before:**
```kotlin
// Old WebView approach
val webView = WebView(context).apply {
    settings.javaScriptEnabled = true
    loadUrl(checkoutUrl)
}
```

**After:**
```kotlin
// New Cashia SDK approach
CashiaCheckoutDialog(
    showDialog = true,
    checkoutRequest = checkoutRequest,
    onResult = { result -> }
)
```

### From Iframe HTML

If you're loading HTML with an iframe:

**Before:**
```kotlin
webView.loadData("""
    <iframe src="$checkoutUrl" width="100%" height="100%"></iframe>
""", "text/html", null)
```

**After:**
```kotlin
// Create session and show checkout
CashiaCheckoutDialog(
    showDialog = true,
    checkoutRequest = CheckoutSessionRequest(
        // Your parameters
    ),
    onResult = { result -> }
)
```

## Best Practices

### 1. Error Handling

Always handle all result types:

```kotlin
onResult = { result ->
    when (result) {
        is CheckoutResult.Success -> { /* Handle success */ }
        is CheckoutResult.Failed -> { /* Handle failure */ }
        is CheckoutResult.Cancelled -> { /* Handle cancellation */ }
        is CheckoutResult.Error -> { /* Handle error */ }
    }
}
```

### 2. Request ID Generation

Use unique, traceable request IDs:

```kotlin
val requestId = "order-${userId}-${System.currentTimeMillis()}"
```

### 3. Amount Handling

Always use smallest currency unit (cents):

```kotlin
// Correct
amount = 1999  // $19.99

// Incorrect
amount = 19.99  // Will cause type error
```

### 4. Testing

Test with different scenarios:

```kotlin
// Test successful payment
val testSuccessRequest = CheckoutSessionRequest(/* ... */)

// Test cancellation
// User presses back button

// Test network error
// Disconnect internet before checkout

// Test invalid credentials
// Use wrong API keys
```

### 5. Memory Management

Properly manage checkout state:

```kotlin
var showCheckout by remember { mutableStateOf(false) }

// Always reset state after handling result
onResult = { result ->
    handleResult(result)
    showCheckout = false  // Important!
}
```

### 6. Security

- Never log sensitive data (API keys, secrets)
- Use ProGuard/R8 for release builds
- Validate amounts on your server
- Implement webhook signature verification

## Troubleshooting

### Common Issues

**Issue**: Checkout not showing  
**Solution**: Check that SDK is initialized before calling `CashiaCheckoutDialog`

**Issue**: Authentication errors  
**Solution**: Verify API credentials and environment setting

**Issue**: WebView blank screen  
**Solution**: Check internet connectivity and clear app data

**Issue**: Crashes on older Android versions  
**Solution**: Ensure minSdk is set to 24 or higher

## Support

For additional help:
- Documentation: https://developer.cashia.com
- Email: developers@cashia.com
- Sample Code: See the `sample` module in this repository