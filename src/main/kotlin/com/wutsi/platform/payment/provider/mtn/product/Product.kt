package com.wutsi.platform.payment.provider.mtn.product

import com.wutsi.platform.payment.provider.mtn.model.TokenResponse

interface Product {
    fun token(): TokenResponse
}
