package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.core.Status.UNKNOWN

data class CreatePaymentResponse(
    val transactionId: String = "",
    val status: Status = UNKNOWN
)
