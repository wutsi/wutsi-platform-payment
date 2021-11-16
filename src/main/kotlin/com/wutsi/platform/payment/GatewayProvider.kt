package com.wutsi.platform.payment

import org.slf4j.LoggerFactory

open class GatewayProvider {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(GatewayProvider::class.java)
    }

    private val gateways: MutableMap<PaymentMethodProvider, Gateway> = mutableMapOf()

    fun register(gateway: Gateway) {
        LOGGER.info("Registering Gateway: ${gateway.provider()}")

        gateways[gateway.provider()] = gateway
    }

    fun get(provider: PaymentMethodProvider) =
        gateways[provider] ?: throw IllegalStateException("Unsupported gateway: $provider")
}