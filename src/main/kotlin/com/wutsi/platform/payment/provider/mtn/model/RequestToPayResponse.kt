package com.wutsi.platform.payment.provider.mtn.model

data class RequestToPayResponse(
    val financialTransactionId: String? = null,
    val status: String = "",
    val reason: String? = null,
    val amount: String = "",
    val currency: String = "",
    val externalId: String = "",
    val payeeNote: String = "",
    val payerMessage: String = "",
    val payer: Party = Party()
)
