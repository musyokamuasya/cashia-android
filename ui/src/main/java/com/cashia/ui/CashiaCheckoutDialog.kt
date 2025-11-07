package com.cashia.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cashia.core.model.CheckoutSessionRequest

/**
 * Composable function to display Cashia checkout in a fullscreen dialog
 *
 * This presents the checkout UI in a modal dialog that covers the entire screen.
 * Similar to how payment sheets work in other SDKs.
 *
 * Example usage:
 * ```
 * var showCheckout by remember { mutableStateOf(false) }
 *
 * CashiaCheckoutDialog(
 *     showDialog = showCheckout,
 *     checkoutRequest = CheckoutSessionRequest(
 *         requestId = "order-123",
 *         currency = "USD",
 *         amount = 1500,
 *         orderDetails = listOf(...)
 *     ),
 *     onResult = { result ->
 *         when (result) {
 *             is CheckoutResult.Success -> {
 *                 // Handle successful payment
 *             }
 *             is CheckoutResult.Failed -> {
 *                 // Handle failed payment
 *             }
 *             is CheckoutResult.Cancelled -> {
 *                 // Handle cancellation
 *             }
 *             is CheckoutResult.Error -> {
 *                 // Handle error
 *             }
 *         }
 *         showCheckout = false
 *     }
 * )
 * ```
 *
 * @param showDialog Whether to show the dialog
 * @param checkoutRequest The checkout session request parameters
 * @param onResult Callback invoked when checkout is completed, cancelled, or fails
 */
@Composable
fun CashiaCheckoutDialog(
    showDialog: Boolean,
    checkoutRequest: CheckoutSessionRequest,
    onResult: (CheckoutResult) -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { onResult(CheckoutResult.Cancelled) },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            CashiaCheckout(
                checkoutRequest = checkoutRequest,
                onResult = onResult,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}