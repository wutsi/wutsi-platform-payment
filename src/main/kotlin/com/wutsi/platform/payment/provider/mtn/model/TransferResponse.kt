package com.wutsi.platform.payment.provider.mtn.model

data class TransferResponse(
    val amount: String = "",
    val currency: String = "",
    val financialTransactionId: String = "",
    val payee: Party = Party(),
    val payeeNote: String = "",
    val payerMessage: String = "",
    val externalId: String = "",
    val status: String = "",
    val reason: String? = null
)
