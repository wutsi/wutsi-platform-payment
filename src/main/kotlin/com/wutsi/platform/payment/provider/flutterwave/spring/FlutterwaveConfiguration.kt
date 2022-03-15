package com.wutsi.platform.payment.provider.flutterwave.spring

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.payment.GatewayProvider
import com.wutsi.platform.payment.PaymentMethodProvider
import com.wutsi.platform.payment.core.DefaultHttpListener
import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.flutterwave.FWGateway
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.platform.payment.flutterwave.enabled"],
    havingValue = "true"
)
open class FlutterwaveConfiguration(
    private val gatewayProvider: GatewayProvider,

    @Value("\${wutsi.platform.payment.flutterwave.secret-key}") private val secretKey: String,
) {
    @Bean
    open fun fwGateway(): FWGateway {
        val gateway = FWGateway(fwHttp(), secretKey)
        gatewayProvider.register(PaymentMethodProvider.ORANGE, gateway)
        gatewayProvider.register(PaymentMethodProvider.MTN, gateway)
        return gateway
    }

    @Bean
    open fun fwHttp(): Http {
        return Http(
            client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(),
            objectMapper = ObjectMapper(),
            listener = DefaultHttpListener()
        )
    }
}
