package com.cashia.core.api

import com.cashia.core.config.CashiaConfiguration
import com.cashia.core.auth.HmacAuthHelper
import com.cashia.core.model.CheckoutSessionRequest
import com.cashia.core.model.CheckoutSessionResponse
import com.cashia.core.model.CheckoutSessionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * API client for Cashia hosted checkout
 */
class CashiaApiClient(private val configuration: CashiaConfiguration) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val hmacHelper = HmacAuthHelper(configuration.secretKey)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    /**
     * Create a checkout session
     *
     * @param request Checkout session request details
     * @return Result containing session response or error
     */
    suspend fun createCheckoutSession(
        request: CheckoutSessionRequest
    ): CheckoutSessionResult = withContext(Dispatchers.IO) {
        try {
            val endpoint = "/api/v1/hosted-checkout"
            val fullUrl = "${configuration.environment.baseUrl}$endpoint"
            val parsedUrl = URL(fullUrl)
            val host = "https://" + parsedUrl.host

            val requestBody = json.encodeToString(request)
            val authHeaders = hmacHelper.generateAuthHeaders(
                host = host,
                method = "POST",
                keyId = configuration.keyId,
                requestBody = requestBody
            )

            val httpRequest = Request.Builder()
                .url(fullUrl)
                .apply {
                    authHeaders.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val sessionResponse = json.decodeFromString<CheckoutSessionResponse>(responseBody)
                CheckoutSessionResult.Success(sessionResponse)
            } else {
                CheckoutSessionResult.Error(
                    exception = IOException("HTTP ${response.code}"),
                    message = "Failed to create checkout session: $responseBody"
                )
            }
        } catch (e: Exception) {
            CheckoutSessionResult.Error(
                exception = e,
                message = "Error creating checkout session: ${e.message}"
            )
        }
    }
}