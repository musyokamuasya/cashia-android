package com.cashia.core

import android.content.Context
import com.cashia.core.api.CashiaApiClient
import com.cashia.core.config.CashiaConfiguration
import com.cashia.core.model.CheckoutSessionRequest
import com.cashia.core.model.CheckoutSessionResult

/**
 * Main entry point for Cashia Checkout SDK
 *
 * Initialize this in your Application class:
 * ```
 * Cashia.initialize(
 *     context = this,
 *     configuration = CashiaConfiguration(
 *         keyId = "your_key_id",
 *         secretKey = "your_secret_key",
 *         environment = CashiaConfiguration.CashiaEnvironment.STAGING
 *     )
 * )
 * ```
 */
object Cashia {

    private var apiClient: CashiaApiClient? = null
    private var configuration: CashiaConfiguration? = null

    /**
     * Initialize the Cashia SDK
     *
     * @param context Application context
     * @param configuration Cashia configuration
     */
    fun initialize(context: Context, configuration: CashiaConfiguration) {
        this.configuration = configuration
        this.apiClient = CashiaApiClient(configuration)
    }

    /**
     * Get the API client instance
     *
     * @throws IllegalStateException if SDK is not initialized
     */
    internal fun getApiClient(): CashiaApiClient {
        return apiClient ?: throw IllegalStateException(
            "Cashia SDK not initialized. Call Cashia.initialize() first."
        )
    }

    /**
     * Get the current configuration
     *
     * @throws IllegalStateException if SDK is not initialized
     */
    internal fun getConfiguration(): CashiaConfiguration {
        return configuration ?: throw IllegalStateException(
            "Cashia SDK not initialized. Call Cashia.initialize() first."
        )
    }

    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean {
        return apiClient != null && configuration != null
    }

    /**
     * Create a checkout session
     *
     * @param request Checkout session request
     * @return Result of session creation
     */
    suspend fun createCheckoutSession(
        request: CheckoutSessionRequest
    ): CheckoutSessionResult {
        return getApiClient().createCheckoutSession(request)
    }
}