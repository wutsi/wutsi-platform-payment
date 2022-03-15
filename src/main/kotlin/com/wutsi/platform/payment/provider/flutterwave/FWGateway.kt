package com.wutsi.platform.payment.provider.flutterwave

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.PaymentMethodType
import com.wutsi.platform.payment.core.Error
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.core.HttpException
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreatePaymentResponse
import com.wutsi.platform.payment.model.CreateTransferRequest
import com.wutsi.platform.payment.model.CreateTransferResponse
import com.wutsi.platform.payment.model.GetFeesRequest
import com.wutsi.platform.payment.model.GetFeesResponse
import com.wutsi.platform.payment.model.GetPaymentResponse
import com.wutsi.platform.payment.model.GetTransferResponse
import com.wutsi.platform.payment.model.Party
import com.wutsi.platform.payment.provider.flutterwave.model.FWChargeRequest
import com.wutsi.platform.payment.provider.flutterwave.model.FWFeeRequest
import com.wutsi.platform.payment.provider.flutterwave.model.FWResponse
import com.wutsi.platform.payment.provider.flutterwave.model.FWTransferRequest
import java.util.UUID

open class FWGateway(
    private val http: Http,
    private val secretKey: String
) : Gateway {
    companion object {
        const val BASE_URI = "https://api.flutterwave.com/v3"
    }

    override fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {
        try {
            val response = http.post(
                referenceId = request.externalId,
                uri = "$BASE_URI/charges?type=" + toChargeType(request),
                requestPayload = FWChargeRequest(
                    amount = request.amount.value,
                    currency = request.amount.currency,
                    email = request.payer.email,
                    tx_ref = request.externalId,
                    phone_number = request.payer.phoneNumber,
                    fullname = request.payer.fullName
                ),
                responseType = FWResponse::class.java,
                headers = toHeaders()
            )!!

            val status = toStatus(response)
            if (status == Status.FAILED)
                throw toPaymentException(response)
            else {
                val id = response.data?.id
                return CreatePaymentResponse(
                    transactionId = id?.toString() ?: "",
                    financialTransactionId = response.data?.flw_ref,
                    status = toStatus(response),
                    fees = response.data?.app_fee ?: 0.0
                )
            }
        } catch (ex: HttpException) {
            throw handleException(ex)
        }
    }

    override fun getPayment(transactionId: String): GetPaymentResponse {
        val response = http.get(
            referenceId = transactionId,
            uri = "$BASE_URI/charges/$transactionId",
            responseType = FWResponse::class.java,
            headers = toHeaders()
        )

        val status = toStatus(response!!)
        val data = response.data
        if (status == Status.FAILED)
            throw toPaymentException(response)
        else {
            return GetPaymentResponse(
                amount = Money(
                    value = data?.amount ?: 0.0,
                    currency = data?.currency ?: ""
                ),
                status = toStatus(response),
                description = data?.narration ?: "",
                payer = Party(
                    fullName = data?.full_name ?: "",
                    phoneNumber = data?.account_number ?: ""
                ),
                fees = data?.app_fee ?: 0.0,
                externalId = data?.tx_ref ?: "",
                financialTransactionId = data?.flw_ref
            )
        }
    }

    override fun createTransfer(request: CreateTransferRequest): CreateTransferResponse {
        try {
            val response = http.post(
                referenceId = request.externalId,
                uri = "$BASE_URI/transfers",
                requestPayload = FWTransferRequest(
                    amount = request.amount.value,
                    currency = request.amount.currency,
                    account_number = toPhoneNumber(request.payee.phoneNumber),
                    beneficiary_name = request.payee.fullName,
                    account_bank = toAccountBank(request.amount.currency),
                    narration = request.description,
                    reference = request.externalId
                ),
                responseType = FWResponse::class.java,
                headers = toHeaders()
            )!!

            val status = toStatus(response)
            if (status == Status.FAILED)
                throw toPaymentException(response)
            else {
                return CreateTransferResponse(
                    transactionId = response.data?.id?.toString() ?: "",
                    financialTransactionId = null,
                    status = toStatus(response),
                    fees = response.data?.fee ?: 0.0
                )
            }
        } catch (ex: HttpException) {
            throw handleException(ex)
        }
    }

    override fun getTransfer(transactionId: String): GetTransferResponse {
        val response = http.get(
            referenceId = transactionId,
            uri = "$BASE_URI/transfers/$transactionId",
            responseType = FWResponse::class.java,
            headers = toHeaders()
        )

        val status = toStatus(response!!)
        val data = response.data
        if (status == Status.FAILED)
            throw toPaymentException(response)
        else {
            return GetTransferResponse(
                amount = Money(
                    value = data?.amount ?: 0.0,
                    currency = data?.currency ?: ""
                ),
                status = toStatus(response),
                description = data?.narration ?: "",
                payee = Party(
                    fullName = data?.full_name ?: "",
                    phoneNumber = data?.account_number ?: ""
                ),
                fees = data?.fee ?: 0.0
            )
        }
    }

    override fun getFees(request: GetFeesRequest): GetFeesResponse {
        try {
            val response = http.post(
                referenceId = UUID.randomUUID().toString(),
                uri = "$BASE_URI/transactions/fee",
                requestPayload = FWFeeRequest(
                    amount = request.amount.value,
                    currency = request.amount.currency,
                    type = toFeeType(request)
                ),
                responseType = FWResponse::class.java,
                headers = toHeaders()
            )

            val data = response?.data
            return GetFeesResponse(
                amount = Money(
                    value = data?.amount ?: 0.0,
                    currency = data?.currency ?: ""
                ),
                fees = Money(
                    value = data?.fee ?: 0.0,
                    currency = data?.currency ?: ""
                )
            )
        } catch (ex: HttpException) {
            throw handleException(ex)
        }
    }

    private fun toFeeType(request: GetFeesRequest): String = when (request.paymentMethodType) {
        PaymentMethodType.MOBILE -> "mobilemoney"
        else -> throw IllegalStateException("Unsupported payment method type: ${request.paymentMethodType}")
    }

    private fun toSupplyErrorCode(response: FWResponse): String? =
        response.code

    /** See https://developer.flutterwave.com/docs/integration-guides/errors/ */
    private fun toErrorCode(response: FWResponse): ErrorCode = when (response.message) {
        "DECLINED" -> ErrorCode.DECLINED
        "INSUFFICIENT_FUNDS" -> ErrorCode.NOT_ENOUGH_FUNDS
        "ABORTED" -> ErrorCode.ABORTED
        "CANCELLED" -> ErrorCode.CANCELLED
        "SYSTEM_ERROR" -> ErrorCode.INTERNAL_PROCESSING_ERROR
        "AUTHENTICATION_FAILED" -> ErrorCode.AUTHENTICATION_FAILED
        "Transaction has been flagged as fraudulent" -> ErrorCode.FRAUDULENT
        else -> ErrorCode.UNEXPECTED_ERROR
    }

    private fun toHeaders() = mapOf(
        "Authorization" to "Bearer $secretKey"
    )

    private fun toPhoneNumber(number: String): String =
        if (number.startsWith("+"))
            number.substring(1)
        else
            number

    private fun toAccountBank(currency: String): String = when (currency) {
        "XAF" -> "FMM"
        "XOF" -> "FMM"
        else -> throw PaymentException(Error(code = ErrorCode.INVALID_CURRENCY))
    }

    private fun toChargeType(request: CreatePaymentRequest): String = when (request.amount.currency) {
        "XAF" -> "mobile_money_franco"
        "XOF" -> "mobile_money_franco"
        else -> throw PaymentException(Error(code = ErrorCode.INVALID_CURRENCY))
    }

    private fun toStatus(response: FWResponse): Status = when (response.status) {
        "error" -> Status.FAILED
        "success" -> when (response.data?.status?.lowercase()) {
            "new" -> Status.PENDING
            "pending" -> Status.PENDING
            "successful" -> Status.SUCCESSFUL
            "failed" -> Status.FAILED
            else -> throw IllegalStateException("Status not supported: ${response.data?.status}")
        }
        else -> throw IllegalStateException("Status not supported: ${response.status}")
    }

    private fun handleException(ex: HttpException): Throwable {
        val response = ObjectMapper().readValue(ex.bodyString, FWResponse::class.java)
        return toPaymentException(response, ex)
    }

    private fun toPaymentException(response: FWResponse, ex: Throwable? = null) =
        PaymentException(
            error = Error(
                transactionId = response.data?.id?.toString() ?: "",
                code = toErrorCode(response),
                supplierErrorCode = toSupplyErrorCode(response),
                message = response.message,
                errorId = response.error_id
            ),
            ex
        )
}
