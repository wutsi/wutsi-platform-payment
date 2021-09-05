package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.core.Money

data class PayRequest(
    val payer: Party,
    val amount: Money,
    val invoiceId: String?,
    val description: String?,
    val payerMessage: String?,
)
