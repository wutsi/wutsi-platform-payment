package com.wutsi.platform.payment.provider.mtn.spring

import com.wutsi.platform.payment.provider.mtn.product.Product
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

class MTNProductHealthIndicator(
    private val collection: Product
) : HealthIndicator {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(MTNProductHealthIndicator::class.java)
    }

    override fun health(): Health {
        try {
            val now = System.currentTimeMillis()
            collection.token()
            return Health.up()
                .withDetail("durationMillis", System.currentTimeMillis() - now)
                .build()
        } catch (ex: Exception) {
            LOGGER.error("Health failure", ex)
            return Health.down()
                .withException(ex)
                .build()
        }
    }
}
