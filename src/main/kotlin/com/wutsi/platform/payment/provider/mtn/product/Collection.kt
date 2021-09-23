package com.wutsi.platform.payment.provider.mtn.product

import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.model.RequestToPayRequest
import com.wutsi.platform.payment.provider.mtn.model.RequestToPayResponse

open class Collection(
    config: ProductConfig,
    http: Http
) : AbstractProduct(config, http) {
    fun requestToPay(
        referenceId: String,
        accessToken: String,
        request: RequestToPayRequest
    ) {
        http.post(
            referenceId = referenceId,
            uri = uri("v1_0/requesttopay"),
            requestPayload = request,
            headers = headers(referenceId, accessToken),
            responseType = Any::class.java
        )
    }

    fun requestToPay(referenceId: String, accessToken: String) =
        http.get(
            referenceId = referenceId,
            uri = uri("v1_0/requesttopay/$referenceId"),
            headers = headers(null, accessToken),
            responseType = RequestToPayResponse::class.java
        )!!

    override fun uri(path: String): String =
        config.environment.baseUrl + "/collection/$path"
}
