package com.wutsi.platform.payment.provider.flutterwave.model

data class FWTransferRequest(
    val amount: Double,
    val currency: String,
    val account_bank: String,
    val account_number: String,
    val beneficiary_name: String = "",
    val narration: String = "",
    val reference: String = "",
    val meta: FWMetadata? = null,
)
