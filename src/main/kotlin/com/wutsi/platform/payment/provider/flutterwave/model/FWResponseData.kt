package com.wutsi.platform.payment.provider.flutterwave.model

data class FWResponseData(
    val id: Long = -1,
    val account_number: String = "",
    val bank_code: String = "",
    val full_name: String = "",
    val created_at: String = "",
    val currency: String = "",
    val amount: Double = 0.0,
    val fee: Double = 0.0,
    val status: String = "",
    val reference: String = "",
    val meta: FWMetadata? = null,
    val narration: String? = "",
    val complete_message: String = "",
    val requires_approval: Int = 0,
    val is_approved: Int = 0,
    val bank_name: String? = null,
    val tx_ref: String = "",
    val flw_ref: String? = null,
    val app_fee: Double = 0.0,
    val merchant_fee: Double = 0.0,
    val fraud_status: String? = null,
    val charge_type: String? = null,
    val processor_response: String? = null
)
