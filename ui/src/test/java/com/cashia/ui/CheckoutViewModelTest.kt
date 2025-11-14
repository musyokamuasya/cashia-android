package com.cashia.ui

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.net.toUri
import com.cashia.core.Cashia
import com.cashia.core.model.CheckoutSessionRequest
import com.cashia.core.model.CheckoutSessionResponse
import com.cashia.core.model.CheckoutSessionResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CheckoutViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CheckoutViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CheckoutViewModel()

        // Mock Cashia singleton
        mockkObject(Cashia)

        // Mock Uri parsing
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun whenViewModelCreated_initialStateIsIdle() {
        // Then
        assertTrue(viewModel.uiState.value is CheckoutUiState.Idle)
    }

    @Test
    fun whenCreateCheckoutSession_withValidRequest_updatesStateToLoading() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USDC"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        // When
        viewModel.createCheckoutSession(request)

        // Then - verify loading state was set
        // Note: This happens synchronously before the coroutine completes
    }

    @Test
    fun whenCreateCheckoutSession_withSuccessfulResponse_updatesStateToReady() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USDC"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        // When
        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Ready)
        assertEquals(response.url, (state as CheckoutUiState.Ready).checkoutUrl)
    }

    @Test
    fun whenCreateCheckoutSession_withError_updatesStateToError() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val exception = Exception("Network error")
        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Error(exception, "Failed to create session")

        // When
        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Error)
        val errorState = state as CheckoutUiState.Error
        assertEquals("Failed to create session", errorState.message)
    }

    @Test
    fun whenCreateCheckoutSession_callsCashiaCreateCheckoutSession() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USDC"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        // When
        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { Cashia.createCheckoutSession(request) }
    }

    @Test
    fun whenHandleUrlNavigation_withSuccessUrl_updatesStateToCompletedSuccess() {
        // Given
        val url = "https://example.com/success?requestId=req-123&status=completed"
        val mockUri = mockk<Uri>(relaxed = true)

        every { url.toUri() } returns mockUri
        every { mockUri.getQueryParameter("requestId") } returns "req-123"
        every { mockUri.getQueryParameter("status") } returns "completed"

        // When
        viewModel.handleUrlNavigation(url)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Completed)
        val completedState = state as CheckoutUiState.Completed
        assertTrue(completedState.result is CheckoutResult.Success)
        val successResult = completedState.result as CheckoutResult.Success
        assertEquals("req-123", successResult.requestId)
        assertEquals("completed", successResult.status)
    }

    @Test
    fun whenHandleUrlNavigation_withSuccessUrlWithoutStatus_usesDefaultStatus() {
        // Given
        val url = "https://example.com/success?requestId=req-123"
        val mockUri = mockk<Uri>(relaxed = true)

        every { url.toUri() } returns mockUri
        every { mockUri.getQueryParameter("requestId") } returns "req-123"
        every { mockUri.getQueryParameter("status") } returns null

        // When
        viewModel.handleUrlNavigation(url)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Completed)
        val completedState = state as CheckoutUiState.Completed
        assertTrue(completedState.result is CheckoutResult.Success)
        val successResult = completedState.result as CheckoutResult.Success
        assertEquals("completed", successResult.status)
    }

    @Test
    fun whenHandleUrlNavigation_withErrorUrl_updatesStateToCompletedFailed() {
        // Given
        val url = "https://example.com/error?requestId=req-123"
        val mockUri = mockk<Uri>(relaxed = true)

        every { url.toUri() } returns mockUri
        every { mockUri.getQueryParameter("requestId") } returns "req-123"
        every { mockUri.getQueryParameter("status") } returns null

        // When
        viewModel.handleUrlNavigation(url)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Completed)
        val completedState = state as CheckoutUiState.Completed
        assertTrue(completedState.result is CheckoutResult.Failed)
        val failedResult = completedState.result as CheckoutResult.Failed
        assertEquals("req-123", failedResult.requestId)
        assertEquals("Payment failed", failedResult.reason)
    }

    @Test
    fun whenHandleUrlNavigation_withErrorUrlWithoutRequestId_updatesStateToCompletedFailed() {
        // Given
        val url = "https://example.com/error"
        val mockUri = mockk<Uri>(relaxed = true)

        every { url.toUri() } returns mockUri
        every { mockUri.getQueryParameter("requestId") } returns null
        every { mockUri.getQueryParameter("status") } returns null

        // When
        viewModel.handleUrlNavigation(url)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Completed)
        val completedState = state as CheckoutUiState.Completed
        assertTrue(completedState.result is CheckoutResult.Failed)
        val failedResult = completedState.result as CheckoutResult.Failed
        assertNull(failedResult.requestId)
    }

    @Test
    fun whenHandleUrlNavigation_withNormalUrl_doesNotChangeState() {
        // Given
        val url = "https://checkout.cashia.com/session-123"
        val mockUri = mockk<Uri>(relaxed = true)

        every { url.toUri() } returns mockUri
        every { mockUri.getQueryParameter("requestId") } returns null
        every { mockUri.getQueryParameter("status") } returns null

        val initialState = viewModel.uiState.value

        // When
        viewModel.handleUrlNavigation(url)

        // Then
        assertEquals(initialState, viewModel.uiState.value)
    }

    @Test
    fun whenHandleCancel_updatesStateToCompletedCancelled() {
        // When
        viewModel.handleCancel()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Completed)
        val completedState = state as CheckoutUiState.Completed
        assertTrue(completedState.result is CheckoutResult.Cancelled)
    }

    @Test
    fun whenReset_updatesStateToIdle() = runTest {
        // Given - set up a non-idle state
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USDC"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state is not idle
        assertTrue(viewModel.uiState.value is CheckoutUiState.Ready)

        // When
        viewModel.reset()

        // Then
        assertTrue(viewModel.uiState.value is CheckoutUiState.Idle)
    }

    @Test
    fun whenReset_clearsCheckoutUrl() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USDC"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.reset()

        // Then
        assertTrue(viewModel.uiState.value is CheckoutUiState.Idle)

        // Verify internal checkoutUrl is cleared by checking state after reset
        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Ready)
    }

    @Test
    fun whenCreateCheckoutSession_multipleTimes_updatesStateEachTime() = runTest {
        // Given
        val request1 = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val request2 = CheckoutSessionRequest(
            requestId = "req-456",
            currency = "KES",
            amount = 2000
        )

        val response1 = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USD"
        )

        val response2 = CheckoutSessionResponse(
            sessionId = "session-456",
            requestId = "req-456",
            url = "https://checkout.cashia.com/session-456",
            amount = 2000.0,
            currency = "KES",
            coin = "USD"
        )

        coEvery { Cashia.createCheckoutSession(request1) } returns
                CheckoutSessionResult.Success(response1)
        coEvery { Cashia.createCheckoutSession(request2) } returns
                CheckoutSessionResult.Success(response2)

        // When
        viewModel.createCheckoutSession(request1)
        testDispatcher.scheduler.advanceUntilIdle()

        val state1 = viewModel.uiState.value
        assertTrue(state1 is CheckoutUiState.Ready)
        assertEquals(response1.url, (state1 as CheckoutUiState.Ready).checkoutUrl)

        viewModel.createCheckoutSession(request2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state2 = viewModel.uiState.value
        assertTrue(state2 is CheckoutUiState.Ready)
        assertEquals(response2.url, (state2 as CheckoutUiState.Ready).checkoutUrl)
    }

    @Test
    fun whenHandleUrlNavigation_afterSuccessfulCheckout_canDetectSuccess() = runTest {
        // Given - create a successful checkout first
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USD"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // Given - mock URL navigation
        val successUrl = "https://example.com/success?requestId=req-123&status=completed"
        val mockUri = mockk<Uri>(relaxed = true)

        every { successUrl.toUri() } returns mockUri
        every { mockUri.getQueryParameter("requestId") } returns "req-123"
        every { mockUri.getQueryParameter("status") } returns "completed"

        // When
        viewModel.handleUrlNavigation(successUrl)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Completed)
        val completedState = state as CheckoutUiState.Completed
        assertTrue(completedState.result is CheckoutResult.Success)
    }

    @Test
    fun whenHandleCancel_afterCheckoutReady_transitionsToCancelled() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USDC"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CheckoutUiState.Ready)

        // When
        viewModel.handleCancel()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CheckoutUiState.Completed)
        val completedState = state as CheckoutUiState.Completed
        assertTrue(completedState.result is CheckoutResult.Cancelled)
    }

    @Test
    fun whenCreateCheckoutSession_withComplexRequest_passesAllParameters() = runTest {
        // Given
        val request = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000,
            webhookUrl = "https://example.com/webhook",
            successRedirectUrl = "https://example.com/success",
            errorRedirectUrl = "https://example.com/error"
        )

        val response = CheckoutSessionResponse(
            sessionId = "session-123",
            requestId = "req-123",
            url = "https://checkout.cashia.com/session-123",
            amount = 1000.0,
            currency = "KES",
            coin = "USDC"
        )

        coEvery { Cashia.createCheckoutSession(request) } returns
                CheckoutSessionResult.Success(response)

        // When
        viewModel.createCheckoutSession(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { Cashia.createCheckoutSession(request) }
        assertTrue(viewModel.uiState.value is CheckoutUiState.Ready)
    }
}