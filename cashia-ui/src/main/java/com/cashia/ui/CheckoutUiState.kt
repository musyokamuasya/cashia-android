package com.cashia.ui

/**
 * UI state for the checkout flow
 */
sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object Loading : CheckoutUiState()
    data class Ready(val checkoutUrl: String) : CheckoutUiState()
    data class Completed(val result: CheckoutResult) : CheckoutUiState()
    data class Error(val error: String, val message: String) : CheckoutUiState()
}