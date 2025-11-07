package com.cashia.core.auth

import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

/**
 * Helper class for generating HMAC signatures for Cashia API authentication
 */
class HmacAuthHelper(private val secretKey: String) {

    /**
     * Generate authentication headers for Cashia API request
     *
     * @param host API host (e.g., "staging.cashia.com")
     * @param method HTTP method (e.g., "POST")
     * @param keyId API Key ID
     * @param requestBody JSON request body (for POST/PUT/PATCH)
     * @return Map of authentication headers
     */
    fun generateAuthHeaders(
        host: String,
        method: String,
        keyId: String,
        requestBody: String = ""
    ): Map<String, String> {
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = generateNonce()
        val bodyHash = computeHmacSha256(requestBody, secretKey)

        // Create signing string: host + method + timestamp + nonce + keyId
        val signingString = "$host$method$timestamp$nonce$keyId"
        val signature = computeHmacSha256(signingString, secretKey)

        return mapOf(
            "X-Cashia-Key-ID" to keyId,
            "X-Cashia-Timestamp" to timestamp,
            "X-Cashia-Nonce" to nonce,
            "X-Cashia-Signature" to signature,
            "X-Cashia-Hash" to bodyHash,
            "Content-Type" to "application/json"
        )
    }

    /**
     * Generate a unique random nonce
     */
    private fun generateNonce(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..13)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     * Compute HMAC SHA256 hash
     */
    private fun computeHmacSha256(data: String, key: String): String {
        val hmacSha256 = "HmacSHA256"
        val secretKeySpec = SecretKeySpec(key.toByteArray(), hmacSha256)
        val mac = Mac.getInstance(hmacSha256)
        mac.init(secretKeySpec)
        val hash = mac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}