package com.wutsi.platform.payment

import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreatePaymentResponse
import com.wutsi.platform.payment.model.GetPaymentResponse
import java.io.IOException

interface Gateway {
    fun type(): PaymentMethodType

    fun provider(): PaymentMethodProvider

    @Throws(PaymentException::class, IOException::class)
    fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse

    @Throws(PaymentException::class, IOException::class)
    fun getPayment(transactionId: String): GetPaymentResponse
}
