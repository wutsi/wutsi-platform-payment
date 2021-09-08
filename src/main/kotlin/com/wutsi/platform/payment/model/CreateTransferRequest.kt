package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.core.Money

data class CreateTransferRequest(
    val payee: Party,
    val amount: Money,
    val externalId: String,
    val description: String,
    val payerMessage: String?,
)
