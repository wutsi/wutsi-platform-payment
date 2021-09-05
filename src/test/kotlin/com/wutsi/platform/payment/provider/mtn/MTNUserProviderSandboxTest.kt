package com.wutsi.platform.payment.provider.mtn

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class MTNUserProviderSandboxTest {
    @Test
    fun collectionUser() {
        val user = MTNUserProviderSandbox(
            http = Fixtures.createHttp(),
            callbackUrl = "http://127.0.0.1",
            subscriptionKey = Fixtures.COLLECTION_API_SUBSCRIPTION_KEY
        ).get()

        assertNotNull(user.id)
        assertNotNull(user.apiKey)
    }

    @Test
    fun getDisbusmentUser() {
        val user = MTNUserProviderSandbox(
            http = Fixtures.createHttp(),
            callbackUrl = "http://127.0.0.1",
            subscriptionKey = Fixtures.DISBURSEMENT_API_SUBSCRIPTION_KEY
        ).get()

        assertNotNull(user.id)
        assertNotNull(user.apiKey)
    }
}
