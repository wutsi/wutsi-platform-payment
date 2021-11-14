package com.wutsi.platform.payment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.payment.PaymentMethodProvider.ORANGE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GatewayProviderTest {
    @Test
    fun get() {
        val provider = GatewayProvider()
        val gateway = mock<Gateway>()
        doReturn(ORANGE).whenever(gateway).provider()

        provider.register(gateway)

        assertEquals(gateway, provider.get(ORANGE))
    }

    @Test
    fun notSupported() {
        val provider = GatewayProvider()
        assertThrows<IllegalStateException> {
            provider.get(ORANGE)
        }
    }
}
