# Architecture Documentation

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Android Application                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              User Interface Layer                       │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │  MainActivity / Composable Screens               │  │ │
│  │  │  - Shopping Cart                                 │  │ │
│  │  │  - Product Details                               │  │ │
│  │  │  - Checkout Button                               │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                  │
│                            ▼                                  │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Cashia UI Module (cashia-ui)             │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  CashiaCheckoutDialog                            │ │ │
│  │  │  - Fullscreen modal presentation                 │ │ │
│  │  │  - Dialog management                             │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  │                     │                                   │ │
│  │                     ▼                                   │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  CashiaCheckout (Main Composable)                │ │ │
│  │  │  - State management integration                  │ │ │
│  │  │  - Loading view                                  │ │ │
│  │  │  - Error view with retry                         │ │ │
│  │  │  - WebView container                             │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  │                     │                                   │ │
│  │                     ▼                                   │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  CheckoutViewModel                               │ │ │
│  │  │  - CheckoutUiState: StateFlow                    │ │ │
│  │  │  - Session creation orchestration                │ │ │
│  │  │  - URL navigation handling                       │ │ │
│  │  │  - Result state management                       │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  │                     │                                   │ │
│  │                     ▼                                   │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  CheckoutResult (Sealed Class)                   │ │ │
│  │  │  - Success                                       │ │ │
│  │  │  - Failed                                        │ │ │
│  │  │  - Cancelled                                     │ │ │
│  │  │  - Error                                         │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                  │
│                            ▼                                  │
│  ┌────────────────────────────────────────────────────────┐ │
│  │            Cashia Core Module (cashia-core)           │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  Cashia (SDK Entry Point)                        │ │ │
│  │  │  - initialize(context, config)                   │ │ │
│  │  │  - createCheckoutSession(request)                │ │ │
│  │  │  - isInitialized()                               │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  │                     │                                   │ │
│  │                     ▼                                   │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  CashiaApiClient                                 │ │ │
│  │  │  - OkHttp client                                 │ │ │
│  │  │  - JSON serialization                            │ │ │
│  │  │  - Coroutine-based async operations              │ │ │
│  │  │  - Request/Response handling                     │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  │                     │                                   │ │
│  │                     ▼                                   │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  HmacAuthHelper                                  │ │ │
│  │  │  - HMAC SHA256 signature generation              │ │ │
│  │  │  - Nonce generation                              │ │ │
│  │  │  - Header creation                               │ │ │
│  │  │  - Body hash computation                         │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  │                     │                                   │ │
│  │                     ▼                                   │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  Data Models                                     │ │ │
│  │  │  - CheckoutSessionRequest                        │ │ │
│  │  │  - CheckoutSessionResponse                       │ │ │
│  │  │  - OrderDetail                                   │ │ │
│  │  │  - DeliveryDetails                               │ │ │
│  │  │  - CheckoutSessionResult (sealed)                │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │      Network Layer (OkHttp)           │
        │  - HTTPS requests                     │
        │  - Response parsing                   │
        │  - Error handling                     │
        └───────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │      Cashia Backend API               │
        │  - POST /v1/hosted-checkout           │
        │  - HMAC authentication                │
        │  - Session creation                   │
        │  - Checkout URL generation            │
        └───────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │   Cashia Hosted Checkout Page         │
        │  - Payment form                       │
        │  - Payment methods (GPay, Card, etc)  │
        │  - Payment processing                 │
        │  - Success/Error redirects            │
        └───────────────────────────────────────┘
```

## Component Flow

### 1. Initialization Flow

```
Application.onCreate()
    │
    ├─► Cashia.initialize(context, config)
    │       │
    │       ├─► Store configuration
    │       └─► Create CashiaApiClient
    │               │
    │               └─► Initialize OkHttpClient
    │                       └─► Add logging interceptor
    │
    └─► Application ready
```

### 2. Checkout Session Creation Flow

```
User clicks "Checkout"
    │
    ├─► showCheckout = true
    │
    ├─► CashiaCheckoutDialog renders
    │
    ├─► CashiaCheckout composable
    │       │
    │       ├─► CheckoutViewModel.createCheckoutSession(request)
    │       │       │
    │       │       ├─► Update state: Loading
    │       │       │
    │       │       ├─► Cashia.createCheckoutSession(request)
    │       │       │       │
    │       │       │       ├─► CashiaApiClient.createCheckoutSession()
    │       │       │       │       │
    │       │       │       │       ├─► HmacAuthHelper.generateAuthHeaders()
    │       │       │       │       │       │
    │       │       │       │       │       ├─► Generate timestamp
    │       │       │       │       │       ├─► Generate nonce
    │       │       │       │       │       ├─► Compute body hash
    │       │       │       │       │       ├─► Create signing string
    │       │       │       │       │       └─► Compute signature
    │       │       │       │       │
    │       │       │       │       ├─► Create HTTP request
    │       │       │       │       ├─► Add auth headers
    │       │       │       │       └─► Execute request
    │       │       │       │
    │       │       │       └─► Parse response
    │       │       │
    │       │       └─► Update state: Ready(checkoutUrl)
    │       │
    │       └─► Render CheckoutWebView
    │               │
    │               ├─► Load checkout URL
    │               └─► Monitor URL navigation
    │
    └─► User interacts with checkout
```

### 3. Payment Completion Flow

```
User completes payment on Cashia page
    │
    ├─► Cashia redirects to successRedirectUrl
    │
    ├─► WebViewClient.shouldOverrideUrlLoading()
    │       │
    │       └─► CheckoutViewModel.handleUrlNavigation(url)
    │               │
    │               ├─► Parse URL parameters
    │               │   - requestId
    │               │   - status
    │               │
    │               ├─► Determine result type
    │               │   - Success if contains "/success"
    │               │   - Failed if contains "/error"
    │               │
    │               └─► Update state: Completed(result)
    │
    ├─► LaunchedEffect detects state change
    │
    ├─► Call onResult callback
    │       │
    │       └─► Application handles result
    │           - CheckoutResult.Success
    │           - CheckoutResult.Failed
    │           - CheckoutResult.Cancelled
    │           - CheckoutResult.Error
    │
    └─► Dialog dismisses
```

## State Management

### CheckoutUiState State Machine

```
┌─────────┐
│  Idle   │ ─────────────────────────┐
└─────────┘                           │
     │                                │
     │ createCheckoutSession()        │
     ▼                                │
┌──────────┐                          │
│ Loading  │                          │
└──────────┘                          │
     │                                │
     │ Session created successfully   │ reset()
     ▼                                │
┌─────────────────┐                   │
│ Ready(url)      │                   │
└─────────────────┘                   │
     │                                │
     │ URL navigation detected        │
     ▼                                │
┌─────────────────┐                   │
│ Completed(result)│ ─────────────────┘
└─────────────────┘
     │
     │ Error during any step
     ▼
┌─────────────────┐
│ Error(error)    │ ──────────────────┐
└─────────────────┘                   │
     │                                │
     │ retry()                        │
     └────────────────────────────────┘
```

## Data Flow

### Request Data Flow

```
Application
    │
    ├─► CheckoutSessionRequest
    │       ├─ requestId: String
    │       ├─ currency: String
    │       ├─ amount: Int
    │       ├─ orderDetails: List<OrderDetail>
    │       └─ deliveryDetails: DeliveryDetails?
    │
    ▼
CashiaApiClient
    │
    ├─► Serialize to JSON
    │
    ├─► Add HMAC authentication
    │       ├─ x-key-id
    │       ├─ x-timestamp
    │       ├─ x-nonce
    │       ├─ x-signature
    │       └─ x-body-hash
    │
    ▼
HTTP POST → Cashia API
    │
    ▼
CheckoutSessionResponse
    ├─ sessionId: String
    ├─ url: String
    ├─ amount: Int
    ├─ currency: String
    └─ requestId: String
```

### Response Data Flow

```
Cashia Hosted Page
    │
    ├─► Payment completed
    │
    ▼
Redirect to callback URL
    │
    ├─► successRedirectUrl?requestId=xxx&status=C
    │   OR
    └─► errorRedirectUrl?requestId=xxx
    │
    ▼
WebView intercepts
    │
    ▼
Parse URL parameters
    │
    ▼
CheckoutResult
    ├─ Success(requestId, status)
    ├─ Failed(requestId?, reason)
    ├─ Cancelled
    └─ Error(error, message)
    │
    ▼
Application callback
```

## Thread Model

### Asynchronous Operations

```
Main Thread (UI)
    │
    ├─► User interaction
    │       │
    │       └─► Trigger checkout
    │
    ▼
Coroutine (viewModelScope)
    │
    ├─► Network call
    │       │
    │       ├─► withContext(Dispatchers.IO)
    │       │       │
    │       │       ├─► Create HTTP request
    │       │       ├─► Execute network call
    │       │       └─► Parse response
    │       │
    │       └─► Return result
    │
    ▼
StateFlow update
    │
    ▼
Compose recomposition (Main Thread)
    │
    └─► Update UI
```

## Security Architecture

### Authentication Flow

```
1. Request Preparation
   ├─ Generate nonce (random string)
   ├─ Get current timestamp
   └─ Prepare request body JSON

2. Body Hash Creation
   ├─ Compute HMAC-SHA256(requestBody, secretKey)
   └─ Convert to hex string

3. Signing String Creation
   └─ Concatenate: host + method + timestamp + nonce + keyId

4. Signature Creation
   ├─ Compute HMAC-SHA256(signingString, secretKey)
   └─ Convert to hex string

5. Headers Creation
   ├─ x-key-id: keyId
   ├─ x-timestamp: timestamp
   ├─ x-nonce: nonce
   ├─ x-signature: signature
   ├─ x-body-hash: bodyHash
   └─ Content-Type: application/json

6. Request Execution
   └─ Send authenticated request to API
```

## Module Dependencies

```
┌──────────────────┐
│   Application    │
└──────────────────┘
         │
         ├──────────────────┐
         │                  │
         ▼                  ▼
┌──────────────┐    ┌──────────────┐
│  cashia-ui   │───►│ cashia-core  │
└──────────────┘    └──────────────┘
         │                  │
         ▼                  ▼
┌──────────────────────────────────┐
│    Android Framework             │
│ - Compose                        │
│ - ViewModel                      │
│ - Coroutines                     │
└──────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│   Third-party Libraries          │
│ - OkHttp                         │
│ - Kotlinx Serialization          │
│ - Accompanist WebView            │
└──────────────────────────────────┘
```

## Error Handling Strategy

```
Try-Catch Hierarchy
    │
    ├─► Network Errors
    │   ├─ IOException
    │   ├─ SocketTimeoutException
    │   └─ UnknownHostException
    │
    ├─► API Errors
    │   ├─ HTTP 4xx (Client errors)
    │   ├─ HTTP 5xx (Server errors)
    │   └─ Invalid response format
    │
    ├─► Authentication Errors
    │   ├─ Invalid credentials
    │   ├─ Signature mismatch
    │   └─ Expired timestamp
    │
    └─► Application Errors
        ├─ SDK not initialized
        ├─ Invalid request data
        └─ Serialization errors

All errors ────► CheckoutSessionResult.Error
                        │
                        └─► CheckoutUiState.Error
                                    │
                                    └─► User shown error UI
                                            with retry option
```

This architecture provides a clear separation of concerns, robust error handling, and a reactive UI that responds to state changes efficiently.