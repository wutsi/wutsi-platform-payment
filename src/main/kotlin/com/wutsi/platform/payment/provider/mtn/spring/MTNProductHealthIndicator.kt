package com.wutsi.platform.payment.provider.mtn.spring

import com.wutsi.platform.payment.provider.mtn.product.Product
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

class MTNProductHealthIndicator(
    private val collection: Product
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
