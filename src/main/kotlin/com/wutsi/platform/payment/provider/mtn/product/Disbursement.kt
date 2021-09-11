package com.wutsi.platform.payment.provider.mtn.product

import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.model.TransferRequest
import com.wutsi.platform.payment.provider.mtn.model.TransferResponse

open class Disbursement(
    config: ProductConfig,
    http: Http
) : AbstractProduct(config, http) {
    fun transfer(
        referenceId: String,
        accessToken: String,
        request: TransferRequest
    ) {
        http.post(
            uri = uri("v1_0/transfer"),
            requestPayload = request,
            headers = headers(referenceId, accessToken),
            responseType = Any::class.java
        )
    }

    fun transfer(referenceId: String, accessToken: String) =
        http.get(
            uri = uri("v1_0/transfer/$referenceId"),
            headers = headers(referenceId, accessToken),
            responseType = TransferResponse::class.java
        )!!

    override fun uri(path: String): String =
        config.environment.baseUrl + "/disbursement/$path"
}
