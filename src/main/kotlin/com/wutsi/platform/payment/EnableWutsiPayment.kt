package com.wutsi.platform.payment

import com.wutsi.platform.payment.provider.mtn.spring.MTNConfiguration
import com.wutsi.platform.payment.provider.om.spring.OMConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(
    value = [
        MTNConfiguration::class,
        OMConfiguration::class
    ]
)
annotation class EnableWutsiPayment
