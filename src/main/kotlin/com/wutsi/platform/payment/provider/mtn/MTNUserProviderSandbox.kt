package com.wutsi.platform.payment.provider.mtn

import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.model.ApiKeyResponse
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.UUID

class MTNUserProviderSandbox(
    private val subscriptionKey: String,
    private val callbackUrl: String,
    private val http: Http
) : MTNUserProvider {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(MTNUserProviderSandbox::class.java)
    }

    private var user: MTNUser? = null

    override fun get(): MTNUser {
        if (user == null)
            user = createUser()

        return user!!
    }

    private fun createUser(): MTNUser {
        LOGGER.info("Creating Sanbox user....")

        // Create user
        val userId = UUID.randomUUID().toString()
        http.post(
            uri = uri(),
            headers = mapOf(
                "Content-Type" to "application/json",
                "X-Reference-Id" to userId,
                "Ocp-Apim-Subscription-Key" to subscriptionKey
            ),
            requestPayload = mapOf(
                "providerCallbackHost" to URL(callbackUrl).host
            ),
            responseType = Any::class.java
        )
        LOGGER.info("User#$userId created...")

        // Get API Key
        val apiKey = http.post(
            uri = uri("/$userId/apikey"),
            headers = mapOf(
                "Content-Type" to "application/json",
                "Ocp-Apim-Subscription-Key" to subscriptionKey
            ),
            requestPayload = emptyMap<String, String>(),
            responseType = ApiKeyResponse::class.java
        )!!.apiKey

        return MTNUser(id = userId, apiKey = apiKey)
    }

    private fun uri(path: String = ""): String =
        MTNEnvironment.SANDBOX.baseUrl + "/v1_0/apiuser$path"
}
