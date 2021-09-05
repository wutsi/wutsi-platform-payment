package com.wutsi.platform.payment.provider.mtn

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MTNUserProviderProductionTest {
    @Test
    fun get() {
        val user = MTNUserProviderProduction("a", "b").get()

        assertEquals("a", user.id)
        assertEquals("b", user.apiKey)
    }
}
