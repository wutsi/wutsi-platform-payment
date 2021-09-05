package com.wutsi.platform.payment.provider.mtn

class MTNUserProviderProduction(
    userId: String,
    apiKey: String
) : MTNUserProvider {
    private val user = MTNUser(userId, apiKey)

    override fun get(): MTNUser =
        user
}
