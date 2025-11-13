package com.cashia.core.auth

import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test

class HmacAuthHelperTest {

    private lateinit var hmacAuthHelper: HmacAuthHelper
    private val testSecretKey = "test-secret-key-123"
    private val testKeyId = "key-id-456"
    private val testHost = "staging.cashia.com"
    private val testMethod = "POST"

    @Before
    fun setup() {
        hmacAuthHelper = HmacAuthHelper(testSecretKey)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given valid parameters when generating auth headers then all required headers are present`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = "{\"amount\": 100}"
        )

        assertNotNull(headers["X-Cashia-Key-ID"])
        assertNotNull(headers["X-Cashia-Timestamp"])
        assertNotNull(headers["X-Cashia-Nonce"])
        assertNotNull(headers["X-Cashia-Signature"])
        assertNotNull(headers["X-Cashia-Hash"])
        assertNotNull(headers["Content-Type"])
    }

    @Test
    fun `given key ID when generating auth headers then key ID header is set correctly`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId
        )

        assertEquals(testKeyId, headers["X-Cashia-Key-ID"])
    }

    @Test
    fun `given valid parameters when generating auth headers then content type is application json`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId
        )

        assertEquals("application/json", headers["Content-Type"])
    }

    @Test
    fun `given current time when generating auth headers then timestamp is valid unix timestamp`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId
        )

        val timestamp = headers["X-Cashia-Timestamp"]?.toLongOrNull()
        assertNotNull(timestamp)

        val now = System.currentTimeMillis() / 1000
        assertTrue(timestamp!! in (now - 60)..now)
    }

    @Test
    fun `given valid parameters when generating auth headers then nonce is 13 characters long`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId
        )

        val nonce = headers["X-Cashia-Nonce"]
        assertNotNull(nonce)
        assertEquals(13, nonce!!.length)
    }

    @Test
    fun `given valid parameters when generating auth headers then nonce contains only alphanumeric lowercase characters`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId
        )

        val nonce = headers["X-Cashia-Nonce"]
        assertNotNull(nonce)
        assertTrue(nonce!!.matches(Regex("^[a-z0-9]{13}$")))
    }

    @Test
    fun `given multiple calls when generating auth headers then each nonce is unique`() {
        val nonces = mutableSetOf<String>()

        repeat(100) {
            val headers = hmacAuthHelper.generateAuthHeaders(
                host = testHost,
                method = testMethod,
                keyId = testKeyId
            )
            nonces.add(headers["X-Cashia-Nonce"]!!)
        }

        assertEquals(100, nonces.size)
    }

    @Test
    fun `given valid parameters when generating auth headers then signature is 64 character hex string`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId
        )

        val signature = headers["X-Cashia-Signature"]
        assertNotNull(signature)
        assertEquals(64, signature!!.length)
        assertTrue(signature.matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun `given request body when generating auth headers then hash is 64 character hex string`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = "{\"test\": \"data\"}"
        )

        val hash = headers["X-Cashia-Hash"]
        assertNotNull(hash)
        assertEquals(64, hash!!.length)
        assertTrue(hash.matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun `given empty request body when generating auth headers then hash is still valid`() {
        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = "GET",
            keyId = testKeyId,
            requestBody = ""
        )

        val hash = headers["X-Cashia-Hash"]
        assertNotNull(hash)
        assertEquals(64, hash!!.length)
    }

    @Test
    fun `given identical request bodies when generating auth headers then hashes are equal`() {
        val headers1 = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = "{\"amount\": 100}"
        )

        val headers2 = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = "{\"amount\": 100}"
        )

        assertEquals(headers1["X-Cashia-Hash"], headers2["X-Cashia-Hash"])
    }

    @Test
    fun `given different request bodies when generating auth headers then hashes are different`() {
        val headers1 = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = "{\"amount\": 100}"
        )

        val headers2 = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = "{\"amount\": 200}"
        )

        assertNotEquals(headers1["X-Cashia-Hash"], headers2["X-Cashia-Hash"])
    }

    @Test
    fun `given various HTTP methods when generating auth headers then all methods produce valid signatures`() {
        val methods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")

        methods.forEach { method ->
            val headers = hmacAuthHelper.generateAuthHeaders(
                host = testHost,
                method = method,
                keyId = testKeyId
            )

            assertNotNull(headers["X-Cashia-Signature"])
            assertEquals(64, headers["X-Cashia-Signature"]!!.length)
        }
    }

    @Test
    fun `given special characters in request body when generating auth headers then hash is computed correctly`() {
        val specialBody = "{\"text\": \"Hello\\nWorld\\t\\\"Test\\\"\"}"

        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = specialBody
        )

        assertNotNull(headers["X-Cashia-Hash"])
        assertEquals(64, headers["X-Cashia-Hash"]!!.length)
    }

    @Test
    fun `given unicode characters in request body when generating auth headers then hash is computed correctly`() {
        val unicodeBody = "{\"message\": \"Hello 世界 🌍\"}"

        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = unicodeBody
        )

        assertNotNull(headers["X-Cashia-Hash"])
        assertEquals(64, headers["X-Cashia-Hash"]!!.length)
    }

    @Test
    fun `given large request body when generating auth headers then hash is computed correctly`() {
        val largeBody = "{\"data\": \"${"x".repeat(10000)}\"}"

        val headers = hmacAuthHelper.generateAuthHeaders(
            host = testHost,
            method = testMethod,
            keyId = testKeyId,
            requestBody = largeBody
        )

        assertNotNull(headers["X-Cashia-Hash"])
        assertEquals(64, headers["X-Cashia-Hash"]!!.length)
    }
}