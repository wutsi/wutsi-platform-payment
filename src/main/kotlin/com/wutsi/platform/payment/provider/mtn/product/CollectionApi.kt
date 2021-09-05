package com.wutsi.platform.payment.provider.mtn.product

import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.MTNApiConfig
import com.wutsi.platform.payment.provider.mtn.model.RequestToPayRequest
import com.wutsi.platform.payment.provider.mtn.model.RequestToPayResponse

class CollectionApi(
    config: MTNApiConfig,
    http: Http
) : AbstractApi(config, http) {
    fun requestToPay(
        referenceId: String,
        accessToken: String,
        request: RequestToPayRequest
    ) {
        http.post(
            uri = uri("v1_0/requesttopay"),
            requestPayload = request,
            headers = headers(referenceId, accessToken),
            responseType = Any::class.java
        )
    }

    fun requestToPay(referenceId: String, accessToken: String) =
        http.get(
            uri = uri("v1_0/requesttopay/$referenceId"),
            headers = headers(null, accessToken),
            responseType = RequestToPayResponse::class.java
        )!!

    override fun uri(path: String): String =
        config.environment.baseUrl + "/collection/$path"
}
