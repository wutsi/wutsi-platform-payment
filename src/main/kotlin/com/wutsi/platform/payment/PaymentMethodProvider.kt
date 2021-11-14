package com.wutsi.platform.payment

enum class PaymentMethodProvider(val type: PaymentMethodType) {
    UNKNOWN(PaymentMethodType.UNKNOWN),
    MTN(PaymentMethodType.MOBILE),
    ORANGE(PaymentMethodType.MOBILE),
    NEXTTEL(PaymentMethodType.MOBILE),
}
