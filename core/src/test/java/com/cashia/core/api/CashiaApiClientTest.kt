package com.cashia.core.api

import com.cashia.core.auth.HmacAuthHelper
import com.cashia.core.config.CashiaConfiguration
import com.cashia.core.model.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import okhttp3.Call as OkHttpCall

class CashiaApiClientTest {

    private lateinit var apiClient: CashiaApiClient
    private lateinit var mockOkHttpClient: OkHttpClient
    private lateinit var mockCall: OkHttpCall
    private lateinit var configuration: CashiaConfiguration
    private lateinit var hmacHelper: HmacAuthHelper

    private val testKeyId = "test-key-id"
    private val testSecretKey = "test-secret-key"
    private val testMerchantId = "merchant-123"

    @Before
    fun setup() {
        configuration = CashiaConfiguration(
            keyId = testKeyId,
            secretKey = testSecretKey,
            environment = CashiaConfiguration.CashiaEnvironment.STAGING
        )

        mockOkHttpClient = mockk(relaxed = true)
        mockCall = mockk(relaxed = true)
        hmacHelper = mockk(relaxed = true)

        apiClient = CashiaApiClient(configuration)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun whenCreateCheckoutSession_withValidRequest_returnsSuccessResult() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val expectedResponse = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "KES"
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body.string() } returns Json.encodeToString(
            CheckoutSessionResponse.serializer(),
            expectedResponse
        )

        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        val result = apiClient.createCheckoutSession(request)

        // Then
        assertTrue(result is CheckoutSessionResult.Success)
        val successResult = result as CheckoutSessionResult.Success
        assertEquals(expectedResponse.sessionId, successResult.response.sessionId)
        assertEquals(expectedResponse.requestId, successResult.response.requestId)
        assertEquals(expectedResponse.url, successResult.response.url)
        assertEquals(expectedResponse.amount, successResult.response.amount, 0.01)
        assertEquals(expectedResponse.currency, successResult.response.currency)
        assertEquals(expectedResponse.coin, successResult.response.coin)
    }

    @Test
    fun whenCreateCheckoutSession_withValidRequest_callsCorrectEndpoint() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1000.0,
                "currency": "KES",
                "coin": "USD"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        val expectedUrl =
            "${CashiaConfiguration.CashiaEnvironment.STAGING.baseUrl}/api/v1/hosted-checkout"
        assertEquals(expectedUrl, capturedRequest.captured.url.toString())
    }

    @Test
    fun whenCreateCheckoutSession_withValidRequest_usesPostMethod() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1000.0,
                "currency": "KES",
                "coin": "USDC"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        assertEquals("POST", capturedRequest.captured.method)
    }

    @Test
    fun whenCreateCheckoutSession_withValidRequest_includesAuthHeaders() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1000.0,
                "currency": "KES",
                "coin": "USD"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        val headers = capturedRequest.captured.headers
        assertNotNull(headers["X-Cashia-Key-ID"])
        assertNotNull(headers["X-Cashia-Timestamp"])
        assertNotNull(headers["X-Cashia-Nonce"])
        assertNotNull(headers["X-Cashia-Signature"])
        assertNotNull(headers["X-Cashia-Hash"])
        assertNotNull(headers["Content-Type"])
    }

    @Test
    fun whenCreateCheckoutSession_withValidRequest_setsApplicationJsonContentType() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1000.0,
                "currency": "KES",
                "coin": "USD"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        assertEquals("application/json", capturedRequest.captured.headers["Content-Type"])
    }

    @Test
    fun whenCreateCheckoutSession_withWebhookUrl_includesWebhookInRequestBody() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000,
            webhookUrl = "https://example.com/webhook"
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1000.0,
                "currency": "KES",
                "coin": "USD"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        val requestBody = capturedRequest.captured.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }
        assertNotNull(requestBody)
        assertTrue(requestBody!!.contains("webhookUrl"))
        assertTrue(requestBody.contains("https://example.com/webhook"))
    }

    @Test
    fun whenCreateCheckoutSession_withSuccessRedirectUrl_includesSuccessUrlInRequestBody() =
        runTest {
            // Given
            val request = CheckoutSessionRequest(
                requestId = "req-123",
                currency = "KES",
                amount = 1000,
                successRedirectUrl = "https://example.com/success"
            )

            val mockResponse = mockk<Response>(relaxed = true)
            every { mockResponse.isSuccessful } returns true
            every { mockResponse.code } returns 200
            every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1000.0,
                "currency": "KES",
                "coin": "USDC"
            }
        """.trimIndent()

            val capturedRequest = slot<Request>()
            every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
            every { mockCall.execute() } returns mockResponse

            val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
            clientField.isAccessible = true
            clientField.set(apiClient, mockOkHttpClient)

            // When
            apiClient.createCheckoutSession(request)

            // Then
            val requestBody = capturedRequest.captured.body?.let { body ->
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            }
            assertNotNull(requestBody)
            assertTrue(requestBody!!.contains("successRedirectUrl"))
            assertTrue(requestBody.contains("https://example.com/success"))
        }

    @Test
    fun whenCreateCheckoutSession_withErrorRedirectUrl_includesErrorUrlInRequestBody() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000,
            errorRedirectUrl = "https://example.com/error"
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1000.0,
                "currency": "KES",
                "coin": "USDC"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        val requestBody = capturedRequest.captured.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }
        assertNotNull(requestBody)
        assertTrue(requestBody!!.contains("errorRedirectUrl"))
        assertTrue(requestBody.contains("https://example.com/error"))
    }

    @Test
    fun whenCreateCheckoutSession_withOrderDetails_includesOrderDetailsInRequestBody() = runTest {
        // Given
        val orderDetails = listOf(
            OrderDetail(
                name = "Product 1",
                currency = "KES",
                quantity = 2,
                description = "Test product",
                price = 500
            ),
            OrderDetail(
                name = "Product 2",
                currency = "KES",
                quantity = 1,
                description = "Another product",
                price = 1000
            )
        )

        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 2000,
            orderDetails = orderDetails
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 2000.0,
                "currency": "KES",
                "coin": "USDC"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        val requestBody = capturedRequest.captured.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }
        assertNotNull(requestBody)
        assertTrue(requestBody!!.contains("orderDetails"))
        assertTrue(requestBody.contains("Product 1"))
        assertTrue(requestBody.contains("Product 2"))
        assertTrue(requestBody.contains("Test product"))
    }

    @Test
    fun whenCreateCheckoutSession_withDeliveryDetails_includesDeliveryDetailsInRequestBody() =
        runTest {
            // Given
            val deliveryDetails = DeliveryDetails(
                currency = "KES",
                fee = 200
            )

            val request = CheckoutSessionRequest(
                requestId = "req-123",
                currency = "KES",
                amount = 1200,
                deliveryDetails = deliveryDetails
            )

            val mockResponse = mockk<Response>(relaxed = true)
            every { mockResponse.isSuccessful } returns true
            every { mockResponse.code } returns 200
            every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1200.0,
                "currency": "KES",
                "coin": "USDC"
            }
        """.trimIndent()

            val capturedRequest = slot<Request>()
            every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
            every { mockCall.execute() } returns mockResponse

            val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
            clientField.isAccessible = true
            clientField.set(apiClient, mockOkHttpClient)

            // When
            apiClient.createCheckoutSession(request)

            // Then
            val requestBody = capturedRequest.captured.body?.let { body ->
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            }
            assertNotNull(requestBody)
            assertTrue(requestBody!!.contains("deliveryDetails"))
            assertTrue(requestBody.contains("\"fee\":200"))
        }

    @Test
    fun whenCreateCheckoutSession_withCompleteRequest_includesAllFieldsInRequestBody() = runTest {
        // Given
        val orderDetails = listOf(
            OrderDetail(
                name = "Product 1",
                currency = "KES",
                quantity = 1,
                description = "Test product",
                price = 1000
            )
        )

        val deliveryDetails = DeliveryDetails(
            currency = "KES",
            fee = 200
        )

        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1200,
            webhookUrl = "https://example.com/webhook",
            successRedirectUrl = "https://example.com/success",
            errorRedirectUrl = "https://example.com/error",
            orderDetails = orderDetails,
            deliveryDetails = deliveryDetails
        )

        val mockResponse = mockk<Response>(relaxed = true)
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.code } returns 200
        every { mockResponse.body?.string() } returns """
            {
                "sessionId": "session-123",
                "requestId": "req-123",
                "url": "https://checkout.cashia.com/session-123",
                "amount": 1200.0,
                "currency": "KES",
                "coin": "USDC"
            }
        """.trimIndent()

        val capturedRequest = slot<Request>()
        every { mockOkHttpClient.newCall(capture(capturedRequest)) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        val clientField = CashiaApiClient::class.java.getDeclaredField("okHttpClient")
        clientField.isAccessible = true
        clientField.set(apiClient, mockOkHttpClient)

        // When
        apiClient.createCheckoutSession(request)

        // Then
        val requestBody = capturedRequest.captured.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }
        assertNotNull(requestBody)
        assertTrue(requestBody!!.contains("requestId"))
        assertTrue(requestBody.contains("webhookUrl"))
        assertTrue(requestBody.contains("successRedirectUrl"))
        assertTrue(requestBody.contains("errorRedirectUrl"))
        assertTrue(requestBody.contains("orderDetails"))
        assertTrue(requestBody.contains("deliveryDetails"))
    }
}