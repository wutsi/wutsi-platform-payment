package com.wutsi.platform.payment.provider.mtn

import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.PaymentMethodProvider.PAYMENT_METHOD_PROVIDER_MTN
import com.wutsi.platform.payment.PaymentMethodType.PAYMENT_METHOD_TYPE_MOBILE_PAYMENT
import com.wutsi.platform.payment.core.Error
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.ErrorCode.AUTHENTICATION_FAILED
import com.wutsi.platform.payment.core.ErrorCode.UNEXPECTED_ERROR
import com.wutsi.platform.payment.core.HttpException
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.core.Status.STATUS_FAILED
import com.wutsi.platform.payment.core.Status.STATUS_PENDING
import com.wutsi.platform.payment.core.Status.STATUS_SUCCESS
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreatePaymentResponse
import com.wutsi.platform.payment.model.CreateTransferRequest
import com.wutsi.platform.payment.model.CreateTransferResponse
import com.wutsi.platform.payment.model.GetPaymentResponse
import com.wutsi.platform.payment.model.GetTransferResponse
import com.wutsi.platform.payment.provider.mtn.model.Party
import com.wutsi.platform.payment.provider.mtn.model.RequestToPayRequest
import com.wutsi.platform.payment.provider.mtn.model.TransferRequest
import com.wutsi.platform.payment.provider.mtn.product.Collection
import com.wutsi.platform.payment.provider.mtn.product.Disbursment
import org.slf4j.LoggerFactory
import java.util.UUID

class MTNGateway(
    private val collection: Collection,
    private val disbursment: Disbursment
) : Gateway {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(MTNGateway::class.java)
    }

    override fun type() = PAYMENT_METHOD_TYPE_MOBILE_PAYMENT

    override fun provider() = PAYMENT_METHOD_PROVIDER_MTN

    override fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {
        LOGGER.info("Creating payment")

        val transactionId = UUID.randomUUID().toString()
        try {

            val accessToken = collection.token().access_token
            collection.requestToPay(
                referenceId = transactionId,
                accessToken = accessToken,
                request = RequestToPayRequest(
                    amount = request.amount.value.toString(),
                    currency = request.amount.currency,
                    payer = Party(request.payer.phoneNumber),
                    payeeNote = request.description ?: "",
                    externalId = request.externalId ?: "",
                    payerMessage = request.payerMessage ?: ""
                )
            )
            val response = collection.requestToPay(transactionId, accessToken)
            val status = toStatus(response.status)
            if (status == STATUS_PENDING || status == STATUS_SUCCESS)
                return CreatePaymentResponse(
                    transactionId = transactionId,
                    status = toStatus(response.status)
                )
            else
                throw PaymentException(
                    error = Error(
                        code = toErrorCode(response.reason),
                        transactionId = transactionId,
                        supplierErrorCode = response.reason
                    )
                )
        } catch (ex: HttpException) {
            throw handleException(transactionId, ex)
        }
    }

    override fun getPayment(transactionId: String): GetPaymentResponse {
        LOGGER.info("Retrieving payment $transactionId")

        val accessToken = collection.token().access_token
        val response = collection.requestToPay(transactionId, accessToken)
        val status = toStatus(response.status)

        if (status == STATUS_PENDING || status == STATUS_SUCCESS)
            return GetPaymentResponse(
                payer = com.wutsi.platform.payment.model.Party(
                    phoneNumber = response.payer.partyId
                ),
                amount = Money(
                    value = response.amount.toDouble(),
                    currency = response.currency
                ),
                status = status,
                description = response.payeeNote,
                payerMessage = response.payerMessage,
                externalId = response.externalId
            )
        else
            throw PaymentException(
                error = Error(
                    code = toErrorCode(response.reason),
                    transactionId = transactionId,
                    supplierErrorCode = response.reason
                )
            )
    }

    override fun createTransfer(request: CreateTransferRequest): CreateTransferResponse {
        LOGGER.info("Creating transfer")

        val transactionId = UUID.randomUUID().toString()
        try {

            val accessToken = disbursment.token().access_token
            disbursment.transfer(
                referenceId = transactionId,
                accessToken = accessToken,
                request = TransferRequest(
                    amount = request.amount.value.toString(),
                    currency = request.amount.currency,
                    payee = Party(request.payee.phoneNumber),
                    payeeNote = request.description ?: "",
                    externalId = request.externalId ?: "",
                    payerMessage = request.payerMessage ?: ""
                )
            )
            val response = disbursment.transfer(transactionId, accessToken)
            val status = toStatus(response.status)
            if (status == STATUS_PENDING || status == STATUS_SUCCESS)
                return CreateTransferResponse(
                    transactionId = transactionId,
                    status = toStatus(response.status)
                )
            else
                throw PaymentException(
                    error = Error(
                        code = toErrorCode(response.reason),
                        transactionId = transactionId,
                        supplierErrorCode = response.reason
                    )
                )
        } catch (ex: HttpException) {
            throw handleException(transactionId, ex)
        }
    }

    override fun getTransfer(transactionId: String): GetTransferResponse {
        val accessToken = disbursment.token().access_token
        val response = disbursment.transfer(transactionId, accessToken)
        val status = toStatus(response.status)

        if (status == STATUS_PENDING || status == STATUS_SUCCESS)
            return GetTransferResponse(
                payee = com.wutsi.platform.payment.model.Party(
                    phoneNumber = response.payee.partyId
                ),
                amount = Money(
                    value = response.amount.toDouble(),
                    currency = response.currency
                ),
                status = status,
                description = response.payeeNote,
                payerMessage = response.payerMessage,
                externalId = response.externalId
            )
        else
            throw PaymentException(
                error = Error(
                    code = toErrorCode(response.reason),
                    transactionId = transactionId,
                    supplierErrorCode = response.reason
                )
            )
    }

    private fun toStatus(status: String): Status = when (status) {
        "FAILED" -> STATUS_FAILED
        "SUCCESSFUL" -> STATUS_SUCCESS
        "PENDING" -> STATUS_PENDING
        "TIMEOUT" -> STATUS_FAILED
        else -> throw IllegalStateException("Unknown status: $status")
    }

    private fun toErrorCode(reason: String?): ErrorCode =
        MTNErrorCode.values().find { it.name == reason }?.paymentErrorCode ?: UNEXPECTED_ERROR

    private fun handleException(transactionId: String, ex: HttpException): Throwable =
        if (ex.statusCode == 401)
            PaymentException(
                error = Error(
                    code = AUTHENTICATION_FAILED,
                    transactionId = transactionId
                ),
                ex.message,
                ex
            )
        else
            PaymentException(
                error = Error(
                    code = UNEXPECTED_ERROR,
                    transactionId = transactionId
                ),
                ex.message,
                ex
            )
}
