package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.PaymentMethodType
import com.wutsi.platform.payment.core.Money

data class GetFeesRequest(
    val amount: Money,
    val paymentMethodType: PaymentMethodType,
)
