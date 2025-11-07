package com.cashia.checkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cashia.checkout.ui.theme.CashiaCheckoutTheme
import com.cashia.core.model.CheckoutSessionRequest
import com.cashia.core.model.DeliveryDetails
import com.cashia.core.model.OrderDetail
import com.cashia.ui.CashiaCheckoutDialog
import com.cashia.ui.CheckoutResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CashiaCheckoutTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SampleCheckoutScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleCheckoutScreen() {
    var showCheckout by remember { mutableStateOf(false) }
    var checkoutResultMessage by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    val sampleCheckoutRequest = remember {
        CheckoutSessionRequest(
            requestId = "order-2-2${System.currentTimeMillis()}",
            currency = "KES",
            amount = 2, // $90.00
            webhookUrl = "https://ciox-kiosk.com/webhook",
            successRedirectUrl = "https://ciox-kiosk.com/success",
            errorRedirectUrl = "https://ciox-kiosk.com/error",
            orderDetails = listOf(
                OrderDetail(
                    name = "Kicks Kali 2",
                    currency = "KES",
                    quantity = 1,
                    description = "Kicks kali sana",
                    price = 1
                ),
                OrderDetail(
                    name = "Pure glow 2",
                    currency = "KES",
                    quantity = 1,
                    description = "Pure glow skincare jar",
                    price = 1
                )
            ),
            deliveryDetails = DeliveryDetails(
                currency = "KES",
                fee = 0
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cashia Checkout Sample") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Your Cart",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleCheckoutRequest.orderDetails.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${item.quantity} × ${item.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$${item.price / 100.0}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$${sampleCheckoutRequest.amount / 100.0}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Button(
                onClick = { showCheckout = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Proceed to Checkout",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "This will open the Cashia checkout UI",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (checkoutResultMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Last Result:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = checkoutResultMessage!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Cashia Checkout Dialog
    CashiaCheckoutDialog(
        showDialog = showCheckout,
        checkoutRequest = sampleCheckoutRequest,
        onResult = { result ->
            showCheckout = false

            checkoutResultMessage = when (result) {
                is CheckoutResult.Success -> {
                    "✅ Payment successful!\nRequest ID: ${result.requestId}\nStatus: ${result.status}"
                }
                is CheckoutResult.Failed -> {
                    "❌ Payment failed\n${result.reason}"
                }
                is CheckoutResult.Cancelled -> {
                    "⚠️ Checkout cancelled by user"
                }
                is CheckoutResult.Error -> {
                    "🚫 Error: ${result.message}"
                }
            }

            showResultDialog = true
        }
    )

    // Result Dialog
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("Checkout Result") },
            text = { Text(checkoutResultMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { showResultDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}