package com.cashia.checkout


import android.app.Application
import com.cashia.core.Cashia
import com.cashia.core.config.CashiaConfiguration

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Cashia SDK
        Cashia.initialize(
            context = this,
            configuration = CashiaConfiguration(
                keyId = BuildConfig.KEY_ID,
                secretKey = BuildConfig.SECRET_KEY,
                environment = CashiaConfiguration.CashiaEnvironment.STAGING
            )
        )
    }
}