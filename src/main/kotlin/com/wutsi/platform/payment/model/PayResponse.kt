package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.core.Status.STATUS_UNKNOWN

data class PayResponse(
    val transactionId: String = "",
    val status: Status = STATUS_UNKNOWN
)
