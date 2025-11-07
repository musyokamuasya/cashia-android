package com.cashia.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutSessionRequest(
    val requestId: String,
    val currency: String,
    val amount: Int,
    val webhookUrl: String? = null,
    val successRedirectUrl: String? = null,
    val errorRedirectUrl: String? = null,
    val orderDetails: List<OrderDetail> = emptyList(),
    val deliveryDetails: DeliveryDetails? = null
)

@Serializable
data class OrderDetail(
    val name: String,
    val currency: String,
    val quantity: Int,
    val description: String,
    val price: Int
)

@Serializable
data class DeliveryDetails(
    val currency: String,
    val fee: Int
)

@Serializable
data class CheckoutSessionResponse(
    val sessionId: String,
    val requestId: String,
    val url: String,
    val amount: Double,
    val currency: String,
    val coin: String
)

/**
 * Result of checkout session creation
 */
sealed class CheckoutSessionResult {
    data class Success(val response: CheckoutSessionResponse) : CheckoutSessionResult()
    data class Error(val exception: Exception, val message: String) : CheckoutSessionResult()
}