package com.cashia.ui

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashia.core.model.CheckoutSessionRequest


/**
 * Composable function to display Cashia checkout
 *
 * This is the main entry point for presenting the Cashia checkout UI.
 * It handles creating the checkout session and displaying the payment interface.
 *
 * Example usage:
 * ```
 * var showCheckout by remember { mutableStateOf(false) }
 *
 * if (showCheckout) {
 *     CashiaCheckout(
 *         checkoutRequest = CheckoutSessionRequest(
 *             requestId = "order-123",
 *             currency = "USD",
 *             amount = 1500,
 *             orderDetails = listOf(...)
 *         ),
 *         onResult = { result ->
 *             when (result) {
 *                 is CheckoutResult.Success -> {
 *                     // Handle successful payment
 *                 }
 *                 is CheckoutResult.Failed -> {
 *                     // Handle failed payment
 *                 }
 *                 is CheckoutResult.Cancelled -> {
 *                     // Handle cancellation
 *                 }
 *                 is CheckoutResult.Error -> {
 *                     // Handle error
 *                 }
 *             }
 *             showCheckout = false
 *         }
 *     )
 * }
 * ```
 *
 * @param checkoutRequest The checkout session request parameters
 * @param onResult Callback invoked when checkout is completed, cancelled, or fails
 * @param modifier Modifier for the composable
 * @param viewModel Optional ViewModel (for testing)
 */
@Composable
fun CashiaCheckout(
    checkoutRequest: CheckoutSessionRequest,
    onResult: (CheckoutResult) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(checkoutRequest) {
        viewModel.createCheckoutSession(checkoutRequest)
    }

    // Handle completion
    LaunchedEffect(uiState) {
        if (uiState is CheckoutUiState.Completed) {
            onResult((uiState as CheckoutUiState.Completed).result)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is CheckoutUiState.Idle -> {
                // Initial state - do nothing
            }
            is CheckoutUiState.Loading -> {
                LoadingView()
            }
            is CheckoutUiState.Ready -> {
                CheckoutWebView(
                    url = state.checkoutUrl,
                    onUrlNavigation = viewModel::handleUrlNavigation,
                )
            }
            is CheckoutUiState.Completed -> {
                // Result will be handled by LaunchedEffect above
            }
            is CheckoutUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.createCheckoutSession(checkoutRequest) },
                    onCancel = viewModel::handleCancel
                )
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading checkout...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutWebView(
    url: String,
    onUrlNavigation: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        setSupportZoom(false)
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            request?.url?.toString()?.let { newUrl ->
                                onUrlNavigation(newUrl)
                            }
                            return false
                        }
                    }

                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}