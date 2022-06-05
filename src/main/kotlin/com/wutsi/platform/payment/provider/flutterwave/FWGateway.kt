package com.wutsi.platform.payment.provider.flutterwave

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.PaymentException
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
import com.wutsi.platform.payment.model.GetPaymentResponse
import com.wutsi.platform.payment.model.GetTransferResponse
import com.wutsi.platform.payment.model.Party
import com.wutsi.platform.payment.provider.flutterwave.FWGateway.Companion.toPaymentException
import com.wutsi.platform.payment.provider.flutterwave.model.FWChargeRequest
import com.wutsi.platform.payment.provider.flutterwave.model.FWResponse
import com.wutsi.platform.payment.provider.flutterwave.model.FWTransferRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

open class FWGateway(
    private val http: Http,
    private val secretKey: String
) : Gateway {
    companion object {
        const val BASE_URI = "https://api.flutterwave.com/v3"
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MILLIS = 1000L
        val LOGGER: Logger = LoggerFactory.getLogger(FWGateway::class.java)

        fun toPaymentException(response: FWResponse, ex: Throwable? = null) = PaymentException(
            error = Error(
                transactionId = response.data?.id?.toString() ?: "",
                code = toErrorCode(response),
                supplierErrorCode = response.code,
                message = response.message,
                errorId = response.error_id
            ),
            ex
        )

        /**
         * See https://developer.flutterwave.com/docs/integration-guides/errors/
         */
        fun toErrorCode(response: FWResponse): ErrorCode = when (response.message) {
            "DECLINED" -> ErrorCode.DECLINED
            "INSUFFICIENT_FUNDS" -> ErrorCode.NOT_ENOUGH_FUNDS
            "ABORTED" -> ErrorCode.ABORTED
            "CANCELLED" -> ErrorCode.CANCELLED
            "SYSTEM_ERROR" -> ErrorCode.INTERNAL_PROCESSING_ERROR
            "AUTHENTICATION_FAILED" -> ErrorCode.AUTHENTICATION_FAILED
            "Transaction has been flagged as fraudulent" -> ErrorCode.FRAUDULENT
            "email is required" -> ErrorCode.EMAIL_MISSING
            else -> ErrorCode.UNEXPECTED_ERROR
        }
    }

    open fun health() {
        fwRetryable { doHealth() }
    }

    private fun doHealth() {
        val from = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        http.get(
            referenceId = UUID.randomUUID().toString(),
            uri = "$BASE_URI/transactions?from=$from",
            responseType = Map::class.java,
            headers = toHeaders(),
        )
    }

    override fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse =
        fwRetryable {
            val fwRequest = FWChargeRequest(
                amount = toAmount(request.amount),
                currency = request.amount.currency,
                email = request.payer.email ?: "",
                tx_ref = request.externalId,
                phone_number = toPhoneNumber(request.payer.phoneNumber),
                country = request.payer.country,
                fullname = request.payer.fullName
            )

            val response = http.post(
                referenceId = request.externalId,
                uri = "$BASE_URI/charges?type=" + toChargeType(request),
                requestPayload = fwRequest,
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
                    fees = Money(response.data?.app_fee ?: 0.0, request.amount.currency)
                )
            }
        }

    override fun getPayment(transactionId: String): GetPaymentResponse =
        fwRetryable {
            val response = http.get(
                referenceId = transactionId,
                uri = "$BASE_URI/transactions/$transactionId/verify",
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
                        fullName = data?.customer?.name ?: "",
                        phoneNumber = data?.customer?.phone_number ?: "",
                        email = data?.customer?.email
                    ),
                    fees = Money(data?.app_fee ?: 0.0, data?.currency ?: ""),
                    externalId = data?.tx_ref ?: "",
                    financialTransactionId = data?.flw_ref
                )
            }
        }

    override fun createTransfer(request: CreateTransferRequest): CreateTransferResponse =
        fwRetryable {
            val payload = FWTransferRequest(
                amount = toAmount(request.amount),
                currency = request.amount.currency,
                account_number = toPhoneNumber(request.payee.phoneNumber),
                beneficiary_name = request.payee.fullName,
                account_bank = toAccountBank(request.amount.currency),
                narration = request.description,
                reference = request.externalId,
                email = request.payee.email ?: "",
            )
            val response = http.post(
                referenceId = request.externalId,
                uri = "$BASE_URI/transfers",
                requestPayload = payload,
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
                    fees = Money(response.data?.fee ?: 0.0, response.data?.currency ?: ""),
                )
            }
        }

    override fun getTransfer(transactionId: String): GetTransferResponse =
        fwRetryable {
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
                    fees = Money(data?.fee ?: 0.0, data?.currency ?: ""),
                    externalId = data?.reference ?: ""
                )
            }
        }

    private fun toHeaders() = mapOf(
        "Authorization" to "Bearer $secretKey",
        "Content-Type" to "application/json"
    )

    private fun toAmount(money: Money): String =
        money.value.toInt().toString()

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
}

/**
 * Inline function to wrap all FW call in a try block
 * - If FW return a plain/text response, this is usually because of connectivity issue => the FW call will be retry (up to 3 time)
 * - If HTTP error return, the response will be parsed an a PaymentException will be thrown
 * - Otherwise, the response is returned
 */
inline fun <T> fwRetryable(bloc: () -> T): T {
    var retry = 0
    while (true) {
        try {
            return bloc()
        } catch (ex: JsonParseException) { // On connectivity error, FW return plain/text response
            FWGateway.LOGGER.warn("$retry - request failed...", ex)
            if (retry++ >= FWGateway.MAX_RETRIES)
                throw ex
            else
                Thread.sleep(FWGateway.RETRY_DELAY_MILLIS) // Pause before re-try
        } catch (ex: HttpException) {
            try {
                val response = ObjectMapper().readValue(ex.bodyString, FWResponse::class.java)
                throw toPaymentException(response, ex)
            } catch (ex1: JsonParseException) {
                throw PaymentException(
                    error = Error(code = ErrorCode.UNEXPECTED_ERROR),
                    cause = ex1
                )
            }
        }
    }
}
