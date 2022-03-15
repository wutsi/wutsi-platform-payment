package com.wutsi.platform.payment.provider.flutterwave.model

data class FWChargeRequest(
    val amount: Double,
    val currency: String,
    val email: String,
    val tx_ref: String,
    val phone_number: String,
    val country: String = "",
    val fullname: String = "",
    val client_ip: String = "",
    val device_fingerprint: String = "",
    val meta: FWMetadata? = null,
)
