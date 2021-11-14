package com.wutsi.platform.payment.provider.om.spring

import com.wutsi.platform.payment.provider.om.OMGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.platform.payment.om.enabled"],
    havingValue = "true"
)
open class OMConfiguration {
    @Bean
    open fun omGateway(): OMGateway =
        OMGateway()
}
