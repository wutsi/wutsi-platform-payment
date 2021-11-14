package com.wutsi.platform.payment

import com.wutsi.platform.payment.provider.mtn.spring.MTNConfiguration
import com.wutsi.platform.payment.provider.om.spring.OMConfiguration
import com.wutsi.platform.payment.spring.GatewayConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(
    value = [
        GatewayConfiguration::class,
        MTNConfiguration::class,
        OMConfiguration::class
    ]
)
annotation class EnableWutsiPayment
