package com.wutsi.platform.payment.provider.flutterwave.model

data class FWFeeRequest(
    val amount: Double,
    val currency: String,
    val type: String,
)
