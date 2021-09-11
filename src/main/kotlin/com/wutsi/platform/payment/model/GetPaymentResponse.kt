package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status

data class GetPaymentResponse(
    val amount: Money = Money(),
    val payer: Party = Party(),
    val status: Status = Status.UNKNOWN,
    val description: String = "",
    val payerMessage: String? = null,
    val externalId: String = ""
)
