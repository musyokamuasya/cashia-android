# Cashia Checkout Android - Quick Start

Get your Cashia checkout running in 5 minutes!

## Step 1: Add Dependencies

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":cashia-core"))
    implementation(project(":cashia-ui"))
}
```

## Step 2: Initialize SDK

In your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Cashia.initialize(
            context = this,
            configuration = CashiaConfiguration(
                keyId = "YOUR_KEY_ID",
                secretKey = "YOUR_SECRET_KEY",
                environment = CashiaConfiguration.CashiaEnvironment.STAGING
            )
        )
    }
}
```

## Step 3: Add to Manifest

```xml
<application
    android:name=".MyApp"
    ...>
```

## Step 4: Show Checkout

```kotlin
@Composable
fun MyScreen() {
    var showCheckout by remember { mutableStateOf(false) }
    
    Button(onClick = { showCheckout = true }) {
        Text("Checkout")
    }
    
    CashiaCheckoutDialog(
        showDialog = showCheckout,
        checkoutRequest = CheckoutSessionRequest(
            requestId = "order-123",
            currency = "USD",
            amount = 5000, // $50.00
            orderDetails = listOf(
                OrderDetail(
                    name = "Product",
                    currency = "USD",
                    quantity = 1,
                    description = "Description",
                    price = 5000
                )
            )
        ),
        onResult = { result ->
            when (result) {
                is CheckoutResult.Success -> {
                    // Payment successful!
                }
                is CheckoutResult.Failed -> {
                    // Payment failed
                }
                is CheckoutResult.Cancelled -> {
                    // User cancelled
                }
                is CheckoutResult.Error -> {
                    // Error occurred
                }
            }
            showCheckout = false
        }
    )
}
```

## Done! 🎉

Your checkout is now integrated.

## Next Steps

- Read the full [README](README.md)
- Check out the [Integration Guide](INTEGRATION_GUIDE.md)
- Run the [Sample App](sample/)

## Need Help?

- Documentation: https://developer.cashia.com
- Email: support@cashia.com