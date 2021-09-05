package com.wutsi.platform.payment

import com.wutsi.platform.payment.model.PayRequest
import com.wutsi.platform.payment.model.PayResponse
import com.wutsi.platform.payment.model.PaymentResponse
import java.io.IOException

interface Gateway {
    fun type(): PaymentMethodType

    fun provider(): PaymentMethodProvider

    @Throws(PaymentException::class, IOException::class)
    fun pay(request: PayRequest): PayResponse

    @Throws(PaymentException::class, IOException::class)
    fun payment(transactionId: String): PaymentResponse
}
