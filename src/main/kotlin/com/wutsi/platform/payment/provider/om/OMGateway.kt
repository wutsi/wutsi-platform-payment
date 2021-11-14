package com.wutsi.platform.payment.provider.om

import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.PaymentMethodProvider.ORANGE
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreatePaymentResponse
import com.wutsi.platform.payment.model.CreateTransferRequest
import com.wutsi.platform.payment.model.CreateTransferResponse
import com.wutsi.platform.payment.model.GetPaymentResponse
import com.wutsi.platform.payment.model.GetTransferResponse
import java.util.UUID

open class OMGateway : Gateway {
    override fun provider() = ORANGE

    override fun createPayment(request: CreatePaymentRequest) = CreatePaymentResponse(
        transactionId = UUID.randomUUID().toString(),
        financialTransactionId = UUID.randomUUID().toString(),
        status = Status.SUCCESSFUL
    )

    override fun getPayment(transactionId: String): GetPaymentResponse = TODO()

    override fun createTransfer(request: CreateTransferRequest) = CreateTransferResponse(
        transactionId = UUID.randomUUID().toString(),
        financialTransactionId = UUID.randomUUID().toString(),
        status = Status.SUCCESSFUL

    )

    override fun getTransfer(transactionId: String): GetTransferResponse = TODO()
}
