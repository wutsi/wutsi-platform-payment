package com.wutsi.platform.payment

import com.wutsi.platform.payment.PaymentMethodProvider.MTN
import com.wutsi.platform.payment.PaymentMethodProvider.NEXTTEL
import com.wutsi.platform.payment.PaymentMethodProvider.ORANGE

enum class Country(
    val code: String,
    val currency: String,
    val paymentProviders: List<PaymentMethodProvider>
) {
    CAMEROON(
        code = "CM",
        currency = "XAF",
        paymentProviders = listOf(
            MTN,
            ORANGE,
            NEXTTEL
        )
    )
}
