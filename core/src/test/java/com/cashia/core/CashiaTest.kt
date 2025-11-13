package com.cashia.core

import android.content.Context
import com.cashia.core.api.CashiaApiClient
import com.cashia.core.config.CashiaConfiguration
import com.cashia.core.model.CheckoutSessionRequest
import com.cashia.core.model.CheckoutSessionResponse
import com.cashia.core.model.CheckoutSessionResult
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CashiaTest {

    private lateinit var mockContext: Context
    private lateinit var mockApiClient: CashiaApiClient
    private lateinit var configuration: CashiaConfiguration

    private val testKeyId = "test-key-id"
    private val testSecretKey = "test-secret-key"

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockApiClient = mockk(relaxed = true)

        configuration = CashiaConfiguration(
            keyId = testKeyId,
            secretKey = testSecretKey,
            environment = CashiaConfiguration.CashiaEnvironment.STAGING
        )

        // Reset Cashia singleton state before each test
        resetCashiaSingleton()
    }

    @After
    fun tearDown() {
        unmockkAll()
        resetCashiaSingleton()
    }

    private fun resetCashiaSingleton() {
        // Use reflection to reset the singleton state
        val apiClientField = Cashia::class.java.getDeclaredField("apiClient")
        apiClientField.isAccessible = true
        apiClientField.set(Cashia, null)

        val configField = Cashia::class.java.getDeclaredField("configuration")
        configField.isAccessible = true
        configField.set(Cashia, null)
    }

    @Test
    fun whenInitialize_withValidConfiguration_setsUpSdk() {
        // When
        Cashia.initialize(mockContext, configuration)

        // Then
        assertTrue(Cashia.isInitialized())
    }

    @Test
    fun whenInitialize_withValidConfiguration_createsApiClient() {
        // When
        Cashia.initialize(mockContext, configuration)

        // Then
        val apiClient = Cashia.getApiClient()
        assertNotNull(apiClient)
    }

    @Test
    fun whenInitialize_withValidConfiguration_storesConfiguration() {
        // When
        Cashia.initialize(mockContext, configuration)

        // Then
        val storedConfig = Cashia.getConfiguration()
        assertEquals(configuration.keyId, storedConfig.keyId)
        assertEquals(configuration.secretKey, storedConfig.secretKey)
        assertEquals(configuration.environment, storedConfig.environment)
    }

    @Test
    fun whenIsInitialized_beforeInitialization_returnsFalse() {
        // When
        val isInitialized = Cashia.isInitialized()

        // Then
        assertFalse(isInitialized)
    }

    @Test
    fun whenIsInitialized_afterInitialization_returnsTrue() {
        // Given
        Cashia.initialize(mockContext, configuration)

        // When
        val isInitialized = Cashia.isInitialized()

        // Then
        assertTrue(isInitialized)
    }


    @Test
    fun whenGetApiClient_beforeInitialization_throwsExceptionWithMessage() {
        // When & Then
        try {
            Cashia.getApiClient()
            fail("Expected IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Cashia SDK not initialized"))
            assertTrue(e.message!!.contains("Call Cashia.initialize() first"))
        }
    }


    @Test
    fun whenGetConfiguration_beforeInitialization_throwsExceptionWithMessage() {
        // When & Then
        try {
            Cashia.getConfiguration()
            fail("Expected IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Cashia SDK not initialized"))
            assertTrue(e.message!!.contains("Call Cashia.initialize() first"))
        }
    }

    @Test
    fun whenGetApiClient_afterInitialization_returnsSameInstance() {
        // Given
        Cashia.initialize(mockContext, configuration)

        // When
        val apiClient1 = Cashia.getApiClient()
        val apiClient2 = Cashia.getApiClient()

        // Then
        assertSame(apiClient1, apiClient2)
    }

    @Test
    fun whenGetConfiguration_afterInitialization_returnsSameInstance() {
        // Given
        Cashia.initialize(mockContext, configuration)

        // When
        val config1 = Cashia.getConfiguration()
        val config2 = Cashia.getConfiguration()

        // Then
        assertSame(config1, config2)
    }

    @Test
    fun whenInitialize_multipleTimes_replacesConfiguration() {
        // Given
        Cashia.initialize(mockContext, configuration)
        val firstConfig = Cashia.getConfiguration()

        val newConfiguration = CashiaConfiguration(
            keyId = "new-key-id",
            secretKey = "new-secret-key",
            environment = CashiaConfiguration.CashiaEnvironment.PRODUCTION
        )

        // When
        Cashia.initialize(mockContext, newConfiguration)

        // Then
        val secondConfig = Cashia.getConfiguration()
        assertNotSame(firstConfig, secondConfig)
        assertEquals("new-key-id", secondConfig.keyId)
        assertEquals(CashiaConfiguration.CashiaEnvironment.PRODUCTION, secondConfig.environment)
    }

    @Test
    fun whenInitialize_withStagingEnvironment_usesCorrectConfiguration() {
        // Given
        val stagingConfig = CashiaConfiguration(
            keyId = testKeyId,
            secretKey = testSecretKey,
            environment = CashiaConfiguration.CashiaEnvironment.STAGING
        )

        // When
        Cashia.initialize(mockContext, stagingConfig)

        // Then
        val storedConfig = Cashia.getConfiguration()
        assertEquals(CashiaConfiguration.CashiaEnvironment.STAGING, storedConfig.environment)
    }

    @Test
    fun whenInitialize_withProductionEnvironment_usesCorrectConfiguration() {
        // Given
        val prodConfig = CashiaConfiguration(
            keyId = testKeyId,
            secretKey = testSecretKey,
            environment = CashiaConfiguration.CashiaEnvironment.PRODUCTION
        )

        // When
        Cashia.initialize(mockContext, prodConfig)

        // Then
        val storedConfig = Cashia.getConfiguration()
        assertEquals(CashiaConfiguration.CashiaEnvironment.PRODUCTION, storedConfig.environment)
    }

    @Test
    fun whenCreateCheckoutSession_withValidRequest_delegatesToApiClient() = runTest {
        // Given
        Cashia.initialize(mockContext, configuration)

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
            coin = "USDC"
        )

        // Mock the API client
        mockkObject(Cashia)
        val mockClient = mockk<CashiaApiClient>()
        every { Cashia.getApiClient() } returns mockClient
        coEvery { mockClient.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(expectedResponse)

        // When
        val result = Cashia.createCheckoutSession(request)

        // Then
        assertTrue(result is CheckoutSessionResult.Success)
        val successResult = result as CheckoutSessionResult.Success
        assertEquals(expectedResponse.sessionId, successResult.response.sessionId)
        coVerify { mockClient.createCheckoutSession(request) }
    }


    @Test
    fun whenInitialize_afterPreviousInitialization_allowsReinitialization() {
        // Given
        Cashia.initialize(mockContext, configuration)
        assertTrue(Cashia.isInitialized())

        val newConfig = CashiaConfiguration(
            keyId = "new-key",
            secretKey = "new-secret",
            environment = CashiaConfiguration.CashiaEnvironment.PRODUCTION
        )

        // When
        Cashia.initialize(mockContext, newConfig)

        // Then
        assertTrue(Cashia.isInitialized())
        assertEquals("new-key", Cashia.getConfiguration().keyId)
        assertEquals(CashiaConfiguration.CashiaEnvironment.PRODUCTION, Cashia.getConfiguration().environment)
    }
}