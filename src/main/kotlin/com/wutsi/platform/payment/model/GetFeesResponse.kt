package com.wutsi.platform.payment.model

import com.wutsi.platform.payment.core.Money

data class GetFeesResponse(
    val amount: Money = Money(),
    val fees: Money = Money(),
)
