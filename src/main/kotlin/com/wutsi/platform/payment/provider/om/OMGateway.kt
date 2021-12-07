package com.wutsi.platform.payment.provider.om

import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.PaymentMethodProvider.ORANGE
import com.wutsi.platform.payment.core.Error
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreatePaymentResponse
import com.wutsi.platform.payment.model.CreateTransferRequest
import com.wutsi.platform.payment.model.CreateTransferResponse
import com.wutsi.platform.payment.model.GetPaymentResponse
import com.wutsi.platform.payment.model.GetTransferResponse
import java.math.BigInteger
import java.util.UUID

open class OMGateway : Gateway {
    override fun provider() = ORANGE

    override fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {
        val transactionId = UUID.randomUUID().toString()
        val status = getStatus(transactionId)
        if (status == Status.FAILED)
            throw PaymentException(
                error = Error(
                    transactionId = transactionId,
                    code = ErrorCode.PAYMENT_NOT_APPROVED,
                    supplierErrorCode = UUID.randomUUID().toString()
                )
            )

        return CreatePaymentResponse(
            transactionId = transactionId,
            financialTransactionId = UUID.randomUUID().toString(),
            status = status
        )
    }

    override fun getPayment(transactionId: String): GetPaymentResponse {
        val status = getStatus(transactionId)
        if (status == Status.FAILED)
            throw PaymentException(
                error = Error(
                    transactionId = transactionId,
                    code = ErrorCode.PAYMENT_NOT_APPROVED,
                    supplierErrorCode = UUID.randomUUID().toString()
                )
            )

        return GetPaymentResponse(
            status = Status.SUCCESSFUL,
            externalId = transactionId,
            financialTransactionId = UUID.randomUUID().toString()
        )
    }

    override fun createTransfer(request: CreateTransferRequest): CreateTransferResponse {
        val transactionId = UUID.randomUUID().toString()
        val status = getStatus(transactionId)
        if (status == Status.FAILED)
            throw PaymentException(
                error = Error(
                    transactionId = transactionId,
                    code = ErrorCode.PAYMENT_NOT_APPROVED,
                    supplierErrorCode = UUID.randomUUID().toString()
                )
            )

        return CreateTransferResponse(
            transactionId = transactionId,
            financialTransactionId = UUID.randomUUID().toString(),
            status = status
        )
    }

    override fun getTransfer(transactionId: String): GetTransferResponse {
        val status = getStatus(transactionId)
        if (status == Status.FAILED)
            throw PaymentException(
                error = Error(
                    transactionId = transactionId,
                    code = ErrorCode.PAYMENT_NOT_APPROVED,
                    supplierErrorCode = UUID.randomUUID().toString()
                )
            )

        return GetTransferResponse(
            status = Status.SUCCESSFUL,
            externalId = transactionId,
            financialTransactionId = UUID.randomUUID().toString()
        )
    }

    private fun getStatus(transactionId: String): Status {
        val bigInt = BigInteger(transactionId, 16)
        val bucket = bigInt.toLong() % 17
        if (bucket < 5) {
            return Status.SUCCESSFUL
        } else if (bucket <= 10) {
            return Status.PENDING
        } else {
            return Status.FAILED
        }
    }
}
