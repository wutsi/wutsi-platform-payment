package com.wutsi.platform.payment.provider.mtn.spring

import com.wutsi.platform.payment.provider.mtn.product.MTNProduct
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

class MTNProductHealthIndicator(
    private val collection: MTNProduct
) : HealthIndicator {
    override fun health(): Health {
        try {
            val now = System.currentTimeMillis()
            collection.token()
            return Health.up()
                .withDetail("durationMillis", System.currentTimeMillis() - now)
                .build()
        } catch (ex: Exception) {
            return Health.down()
                .withException(ex)
                .build()
        }
    }
}
