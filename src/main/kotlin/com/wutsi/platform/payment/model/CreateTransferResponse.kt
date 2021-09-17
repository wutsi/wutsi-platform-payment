package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.core.Status.UNKNOWN

data class CreateTransferResponse(
    val transactionId: String = "",
    val financialTransactionId: String? = null,
    val status: Status = UNKNOWN
)
