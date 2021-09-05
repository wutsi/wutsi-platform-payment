package com.wutsi.platform.payment

import com.wutsi.platform.payment.core.Error

class PaymentException(
    val error: Error = Error(),
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
