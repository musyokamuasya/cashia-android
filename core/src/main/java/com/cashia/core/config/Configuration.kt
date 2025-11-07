package com.cashia.core.config

/**
 * Configuration for Cashia SDK
 *
 * @property keyId Your API Key ID
 * @property secretKey Your API Secret Key
 * @property environment The environment to use (staging or production)
 */
data class CashiaConfiguration(
    val keyId: String,
    val secretKey: String,
    val environment: CashiaEnvironment = CashiaEnvironment.STAGING
) {
    enum class CashiaEnvironment(val baseUrl: String) {
        STAGING("https://staging.cashia.com"),
        PRODUCTION("https://cashia.com")
    }
}