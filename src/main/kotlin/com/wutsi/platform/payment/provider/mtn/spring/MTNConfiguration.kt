package com.wutsi.platform.payment.provider.mtn.spring

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.MTNApiConfig
import com.wutsi.platform.payment.provider.mtn.MTNEnvironment
import com.wutsi.platform.payment.provider.mtn.MTNEnvironment.PRODUCTION
import com.wutsi.platform.payment.provider.mtn.MTNEnvironment.SANDBOX
import com.wutsi.platform.payment.provider.mtn.MTNGateway
import com.wutsi.platform.payment.provider.mtn.MTNUserProvider
import com.wutsi.platform.payment.provider.mtn.MTNUserProviderProduction
import com.wutsi.platform.payment.provider.mtn.MTNUserProviderSandbox
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.net.http.HttpClient.Version.HTTP_1_1
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.platform.payment.provider"],
    havingValue = "mtn"
)
open class MTNConfiguration(
    @Value("\${wutsi.platform.payment.mtn.environment}") private val environment: String,
    @Value("\${wutsi.platform.payment.mtn.collection:subscription-key}") private val collectionSubscriptionKey: String,
    @Value("\${wutsi.platform.payment.mtn.collection:callback-url}") private val collectionCallbackUrl: String,
    @Value("\${wutsi.platform.payment.mtn.collection:user-id:}") private val collectionUserId: String,
    @Value("\${wutsi.platform.payment.mtn.collection:api-key:}") private val collectionApiKey: String
) {
    @Bean
    open fun mtnGateway(): MTNGateway {
        val http = createHttp()
        return MTNGateway(
            http = http,
            collectionConfig = MTNApiConfig(
                environment = mtnEnvironment(),
                subscriptionKey = collectionSubscriptionKey,
                callbackUrl = collectionCallbackUrl,
                userProvider = createUserProvider(
                    userId = collectionUserId,
                    apiKey = collectionApiKey,
                    subscriptionKey = collectionSubscriptionKey,
                    callbackUrl = collectionCallbackUrl,
                    http = http
                )
            )
        )
    }

    private fun createHttp(): Http {
        if (mtnEnvironment() == SANDBOX) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            })

            val context = SSLContext.getInstance("TLS")
            context.init(null, trustAllCerts, SecureRandom())

            System.getProperties().setProperty("jdk.internal.httpclient.disableHostnameVerification", "true")

            return Http(
                client = HttpClient.newBuilder()
                    .version(HTTP_1_1)
                    .sslContext(context)
                    .followRedirects(NORMAL)
                    .build(),
                objectMapper = ObjectMapper()
            )
        } else {
            return Http(
                client = HttpClient.newBuilder()
                    .version(HTTP_1_1)
                    .followRedirects(NORMAL)
                    .build(),
                objectMapper = ObjectMapper()
            )
        }
    }

    private fun createUserProvider(
        userId: String,
        apiKey: String,
        subscriptionKey: String,
        callbackUrl: String,
        http: Http
    ): MTNUserProvider =
        if (mtnEnvironment() == PRODUCTION)
            MTNUserProviderProduction(userId, apiKey)
        else
            MTNUserProviderSandbox(subscriptionKey, callbackUrl, http)

    private fun mtnEnvironment(): MTNEnvironment =
        if (environment.equals("production", ignoreCase = true) || environment.equals("prod", ignoreCase = true))
            PRODUCTION
        else
            SANDBOX
}
