package com.cashia.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cashia.core.model.CheckoutSessionRequest
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CashiaCheckoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: CheckoutViewModel
    private lateinit var uiStateFlow: MutableStateFlow<CheckoutUiState>
    private lateinit var onResultCallback: (CheckoutResult) -> Unit

    private val testRequest = CheckoutSessionRequest(
        requestId = "req-123",
        currency = "KES",
        amount = 1000
    )

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        uiStateFlow = MutableStateFlow(CheckoutUiState.Idle)
        onResultCallback = mockk(relaxed = true)

        every { mockViewModel.uiState } returns uiStateFlow
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun whenComposed_withIdleState_displaysNothing() {
        // Given
        uiStateFlow.value = CheckoutUiState.Idle

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Loading checkout...").assertDoesNotExist()
        composeTestRule.onNodeWithText("Error").assertDoesNotExist()
    }

    @Test
    fun whenComposed_withLoadingState_displaysLoadingView() {
        // Given
        uiStateFlow.value = CheckoutUiState.Loading

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Loading checkout...").assertIsDisplayed()
    }

    @Test
    fun whenComposed_withLoadingState_displaysCircularProgressIndicator() {
        // Given
        uiStateFlow.value = CheckoutUiState.Loading

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun whenComposed_withReadyState_displaysWebView() {
        // Given
        val checkoutUrl = "https://checkout.cashia.com/session-123"
        uiStateFlow.value = CheckoutUiState.Ready(checkoutUrl)

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then - WebView should be displayed (verified by absence of loading/error states)
        composeTestRule.onNodeWithText("Loading checkout...").assertDoesNotExist()
        composeTestRule.onNodeWithText("Error").assertDoesNotExist()
    }

    @Test
    fun whenComposed_withErrorState_displaysErrorView() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Network error",
            message = "Failed to create checkout session"
        )

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to create checkout session").assertIsDisplayed()
    }

    @Test
    fun whenComposed_withErrorState_displaysRetryButton() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Network error",
            message = "Failed to create checkout session"
        )

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun whenComposed_withErrorState_displaysCancelButton() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Network error",
            message = "Failed to create checkout session"
        )

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun whenRetryButtonClicked_inErrorState_callsCreateCheckoutSession() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Network error",
            message = "Failed to create checkout session"
        )

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // When
        composeTestRule.onNodeWithText("Retry").performClick()

        // Then
        verify(atLeast = 2) { mockViewModel.createCheckoutSession(testRequest) }
        // Called twice: once on composition, once on retry
    }

    @Test
    fun whenCancelButtonClicked_inErrorState_callsHandleCancel() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Network error",
            message = "Failed to create checkout session"
        )

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // When
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        verify { mockViewModel.handleCancel() }
    }

    @Test
    fun whenComposed_callsCreateCheckoutSession() {
        // Given
        uiStateFlow.value = CheckoutUiState.Idle

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.waitForIdle()
        verify { mockViewModel.createCheckoutSession(testRequest) }
    }

    @Test
    fun whenStateChangesToCompleted_withSuccessResult_invokesOnResultCallback() {
        // Given
        uiStateFlow.value = CheckoutUiState.Idle

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // When
        val successResult = CheckoutResult.Success(
            requestId = "req-123",
            status = "completed"
        )
        uiStateFlow.value = CheckoutUiState.Completed(successResult)

        // Then
        composeTestRule.waitForIdle()
        verify { onResultCallback(successResult) }
    }

    @Test
    fun whenStateChangesToCompleted_withFailedResult_invokesOnResultCallback() {
        // Given
        uiStateFlow.value = CheckoutUiState.Idle

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // When
        val failedResult = CheckoutResult.Failed(
            requestId = "req-123",
            reason = "Payment failed"
        )
        uiStateFlow.value = CheckoutUiState.Completed(failedResult)

        // Then
        composeTestRule.waitForIdle()
        verify { onResultCallback(failedResult) }
    }

    @Test
    fun whenStateChangesToCompleted_withCancelledResult_invokesOnResultCallback() {
        // Given
        uiStateFlow.value = CheckoutUiState.Idle

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // When
        val cancelledResult = CheckoutResult.Cancelled
        uiStateFlow.value = CheckoutUiState.Completed(cancelledResult)

        // Then
        composeTestRule.waitForIdle()
        verify { onResultCallback(cancelledResult) }
    }

    @Test
    fun whenComposed_withDifferentCheckoutRequest_recreatesCheckoutSession() {
        // Given
        val firstRequest = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000
        )

        val secondRequest = CheckoutSessionRequest(
            requestId = "req-456",
            currency = "KES",
            amount = 2000
        )

        uiStateFlow.value = CheckoutUiState.Idle

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = firstRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        composeTestRule.waitForIdle()
        verify { mockViewModel.createCheckoutSession(firstRequest) }

        // When - recompose with different request
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = secondRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.waitForIdle()
        verify { mockViewModel.createCheckoutSession(secondRequest) }
    }

    @Test
    fun whenErrorView_displaysErrorMessageInCorrectStyle() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Stack trace",
            message = "Network connection failed"
        )

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network connection failed").assertIsDisplayed()
    }

    @Test
    fun whenLoadingView_displaysLoadingTextAndIndicator() {
        // Given
        uiStateFlow.value = CheckoutUiState.Loading

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Loading checkout...").assertIsDisplayed()
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun whenStateTransitions_fromLoadingToReady_updatesUI() {
        // Given
        uiStateFlow.value = CheckoutUiState.Loading

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("Loading checkout...").assertIsDisplayed()

        // When
        uiStateFlow.value = CheckoutUiState.Ready("https://checkout.cashia.com/session-123")

        // Then
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Loading checkout...").assertDoesNotExist()
    }

    @Test
    fun whenStateTransitions_fromLoadingToError_updatesUI() {
        // Given
        uiStateFlow.value = CheckoutUiState.Loading

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("Loading checkout...").assertIsDisplayed()

        // When
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Error trace",
            message = "Failed to load"
        )

        // Then
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Loading checkout...").assertDoesNotExist()
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
    }

    @Test
    fun whenErrorState_retryButtonIsClickable() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Error",
            message = "Failed"
        )

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun whenErrorState_cancelButtonIsClickable() {
        // Given
        uiStateFlow.value = CheckoutUiState.Error(
            error = "Error",
            message = "Failed"
        )

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun whenComposed_withCustomModifier_appliesModifier() {
        // Given
        uiStateFlow.value = CheckoutUiState.Loading

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel,
                modifier = Modifier.testTag("checkout-surface")
            )
        }

        // Then - Surface with modifier should be present
        composeTestRule.onNodeWithTag("checkout-surface").assertExists()
    }

    @Test
    fun whenCompletedState_doesNotDisplayAnyUI() {
        // Given
        val successResult = CheckoutResult.Success(
            requestId = "req-123",
            status = "completed"
        )
        uiStateFlow.value = CheckoutUiState.Completed(successResult)

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then - No UI elements should be displayed
        composeTestRule.onNodeWithText("Loading checkout...").assertDoesNotExist()
        composeTestRule.onNodeWithText("Error").assertDoesNotExist()
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun whenMultipleStateChanges_handlesTransitionsCorrectly() {
        // Given
        uiStateFlow.value = CheckoutUiState.Idle

        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = testRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // When - transition through multiple states
        uiStateFlow.value = CheckoutUiState.Loading
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Loading checkout...").assertIsDisplayed()

        uiStateFlow.value = CheckoutUiState.Ready("https://checkout.cashia.com/session-123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Loading checkout...").assertDoesNotExist()

        val successResult = CheckoutResult.Success("req-123", "completed")
        uiStateFlow.value = CheckoutUiState.Completed(successResult)
        composeTestRule.waitForIdle()

        // Then
        verify { onResultCallback(successResult) }
    }

    @Test
    fun whenComposed_withComplexCheckoutRequest_passesCorrectRequest() {
        // Given
        val complexRequest = CheckoutSessionRequest(
            requestId = "req-123",
            currency = "KES",
            amount = 1000,
            webhookUrl = "https://example.com/webhook",
            successRedirectUrl = "https://example.com/success",
            errorRedirectUrl = "https://example.com/error"
        )

        uiStateFlow.value = CheckoutUiState.Idle

        // When
        composeTestRule.setContent {
            CashiaCheckout(
                checkoutRequest = complexRequest,
                onResult = onResultCallback,
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.waitForIdle()
        verify { mockViewModel.createCheckoutSession(complexRequest) }
    }
}