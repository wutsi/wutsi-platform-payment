package com.wutsi.platform.payment

import com.wutsi.platform.payment.PaymentMethodType.MOBILE

enum class PaymentMethodProvider(
    val displayName: String,
    val paymentType: PaymentMethodType
) {
    UNKNOWN("", PaymentMethodType.UNKNOWN),
    MTN("MTN", MOBILE),
    ORANGE("Orange", MOBILE),
    NEXTTEL("Nexttel", MOBILE)
}
