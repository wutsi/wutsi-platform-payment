package com.wutsi.platform.payment.provider.mtn

data class MTNApiConfig(
    val environment: MTNEnvironment,
    val subscriptionKey: String = "",
    val callbackUrl: String = "",
    val userProvider: MTNUserProvider
)
