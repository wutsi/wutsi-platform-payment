package com.wutsi.platform.payment.provider.om.spring

import com.wutsi.platform.payment.GatewayProvider
import com.wutsi.platform.payment.provider.om.OMGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.platform.payment.om.enabled"],
    havingValue = "true"
)
open class OMConfiguration(
    private val gatewayProvider: GatewayProvider
) {

    @Bean
    open fun omGateway(): OMGateway {
        val gateway = OMGateway()
        gatewayProvider.register(gateway)
        return gateway
    }
}
