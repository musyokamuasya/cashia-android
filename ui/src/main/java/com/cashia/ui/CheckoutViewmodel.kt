package com.cashia.ui

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashia.core.Cashia
import com.cashia.core.model.CheckoutSessionRequest
import com.cashia.core.model.CheckoutSessionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing checkout state
 */
class CheckoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var checkoutUrl: String? = null

    /**
     * Create checkout session and load the checkout URL
     */
    fun createCheckoutSession(request: CheckoutSessionRequest) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Loading

            when (val result = Cashia.createCheckoutSession(request)) {
                is CheckoutSessionResult.Success -> {
                    checkoutUrl = result.response.url
                    _uiState.value = CheckoutUiState.Ready(result.response.url)
                }
                is CheckoutSessionResult.Error -> {
                    _uiState.value = CheckoutUiState.Error(
                        error = result.exception.stackTrace.toString(),
                        message = result.message
                    )
                }
            }
        }
    }

    /**
     * Handle URL navigation to detect success/failure redirects
     */
    fun handleUrlNavigation(url: String) {
        // Parse URL to check for success/error callbacks
        val uri = url.toUri()
        val requestId = uri.getQueryParameter("requestId")
        val status = uri.getQueryParameter("status")

        when {
            url.contains("/success") && requestId != null -> {
                _uiState.value = CheckoutUiState.Completed(
                    CheckoutResult.Success(
                        requestId = requestId,
                        status = status ?: "completed"
                    )
                )
            }
            url.contains("/error") -> {
                _uiState.value = CheckoutUiState.Completed(
                    CheckoutResult.Failed(
                        requestId = requestId,
                        reason = "Payment failed"
                    )
                )
            }
        }
    }

    /**
     * Handle user cancelling the checkout
     */
    fun handleCancel() {
        _uiState.value = CheckoutUiState.Completed(CheckoutResult.Cancelled)
    }

    /**
     * Reset the checkout state
     */
    fun reset() {
        checkoutUrl = null
        _uiState.value = CheckoutUiState.Idle
    }
}
