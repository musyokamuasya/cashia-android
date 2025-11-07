package com.cashia.ui

/**
 * Result of the checkout flow
 */
sealed class CheckoutResult {
    /**
     * Payment was completed successfully
     *
     * @property requestId The request ID from the checkout session
     * @property status Payment status code
     */
    data class Success(
        val requestId: String,
        val status: String
    ) : CheckoutResult()

    /**
     * Payment failed or was cancelled
     *
     * @property requestId The request ID from the checkout session (if available)
     * @property reason Reason for failure/cancellation
     */
    data class Failed(
        val requestId: String? = null,
        val reason: String
    ) : CheckoutResult()

    /**
     * User cancelled the checkout
     */
    object Cancelled : CheckoutResult()

    /**
     * Checkout could not be presented
     *
     * @property error Error that prevented checkout
     * @property message Error message that prevented checkout
     */
    data class Error(
        val error: Throwable,
        val message: String = error.message ?: "Unknown error"
    ) : CheckoutResult()
}