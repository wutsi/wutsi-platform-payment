package com.wutsi.platform.payment.provider.mtn.spring

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.payment.provider.mtn.model.TokenResponse
import com.wutsi.platform.payment.provider.mtn.product.MTNProduct
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status

internal class MTNProductHealthIndicatorTest {
    private lateinit var product: MTNProduct
    private lateinit var hc: HealthIndicator

    @BeforeEach
    fun setUp() {
        product = mock()
        hc = MTNProductHealthIndicator(product)
    }

    @Test
    fun up() {
        doReturn(TokenResponse("")).whenever(product).token()
        val result = hc.health()

        assertEquals(Status.UP, result.status)
    }

    @Test
    fun down() {
        doThrow(RuntimeException::class).whenever(product).token()
        val result = hc.health()

        assertEquals(Status.DOWN, result.status)
    }
}
