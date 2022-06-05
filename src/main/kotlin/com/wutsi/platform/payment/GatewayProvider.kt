package com.wutsi.platform.payment

import org.slf4j.LoggerFactory

open class GatewayProvider {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(GatewayProvider::class.java)
    }

    private val gateways: MutableMap<PaymentMethodProvider, Gateway> = mutableMapOf()

    open fun register(provider: PaymentMethodProvider, gateway: Gateway) {
        LOGGER.info("Registering Gateway: $provider - ${gateway.javaClass.simpleName}")
        if (gateways.containsKey(provider))
            throw IllegalStateException("Gateway already registered fro $provider")
        else
            gateways[provider] = gateway
    }

    open fun get(provider: PaymentMethodProvider) =
        gateways[provider] ?: throw IllegalStateException("Unsupported gateway: $provider")
}
