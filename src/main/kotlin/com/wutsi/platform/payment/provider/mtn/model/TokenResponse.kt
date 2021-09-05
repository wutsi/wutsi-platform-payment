package com.wutsi.platform.payment.provider.mtn.model

data class TokenResponse(
    val access_token: String = "",
    val token_type: String = "",
    val expires_in: Int = -1
)
