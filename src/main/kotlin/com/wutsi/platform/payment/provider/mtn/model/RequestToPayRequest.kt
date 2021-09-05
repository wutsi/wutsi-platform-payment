package com.wutsi.platform.payment.provider.mtn.model

data class RequestToPayRequest(
    val amount: String,
    val currency: String,
    val externalId: String,
    val payeeNote: String,
    val payerMessage: String,
    val payer: Party
)
