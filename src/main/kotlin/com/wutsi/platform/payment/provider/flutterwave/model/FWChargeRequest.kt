package com.wutsi.platform.payment.provider.flutterwave.model

data class FWChargeRequest(
    val amount: String,
    val currency: String,
    val email: String,
    val tx_ref: String,
    val phone_number: String,
    val country: String? = null,
    val fullname: String? = null
)
