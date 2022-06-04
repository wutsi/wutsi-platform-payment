package com.wutsi.platform.payment.provider.flutterwave

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.core.HttpException
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreateTransferRequest
import com.wutsi.platform.payment.model.Party
import com.wutsi.platform.payment.provider.flutterwave.model.FWResponse
import com.wutsi.platform.payment.provider.flutterwave.model.FWResponseData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class FWGatewayTest {
    private val secretKey = "FW_TEST_120909209302"
    private lateinit var http: Http
    private lateinit var gateway: Gateway

    @BeforeEach
    fun setUp() {
        http = mock()
        gateway = FWGateway(http, secretKey)
    }

    @Test
    fun `transfer - success`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "success",
            data = FWResponseData(
                id = id,
                status = "SUCCESSFUL",
                fee = 100.0
            )
        )
        doReturn(resp).whenever(http).post(any(), any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val request = createTransferRequest("237670000001")
        val response = gateway.createTransfer(request)

        // THEN
        assertEquals(Status.SUCCESSFUL, response.status)
        assertEquals(id.toString(), response.transactionId)
        assertNull(response.financialTransactionId)
        assertEquals(resp.data?.fee, response.fees.value)

        val headers = argumentCaptor<Map<String, String>>()
        verify(http).post(
            any(),
            eq("${FWGateway.BASE_URI}/transfers"),
            any(),
            eq(FWResponse::class.java),
            headers.capture()
        )
        assertEquals("Bearer $secretKey", headers.firstValue["Authorization"])
    }

    @Test
    fun `transfer - pending`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "success",
            data = FWResponseData(
                id = id,
                status = "NEW"
            )
        )
        doReturn(resp).whenever(http).post(any(), any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val request = createTransferRequest("237670000001")
        val response = gateway.createTransfer(request)

        // THEN
        assertEquals(Status.PENDING, response.status)
        assertEquals(id.toString(), response.transactionId)
        assertNull(response.financialTransactionId)
    }

    @Test
    fun `transfer - error`() {
        // GIVEN
        val resp = FWResponse(
            status = "error",
            data = null,
            error_id = "FW-1202910329",
            message = "DECLINED",
            code = "err_declined"
        )
        val exception = HttpException(
            statusCode = 500,
            bodyString = ObjectMapper().writeValueAsString(resp)
        )
        doThrow(exception).whenever(http).post(any(), any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val request = createTransferRequest("237670000001")
        val ex = assertThrows<PaymentException> {
            gateway.createTransfer(request)
        }

        // THEN
        assertEquals(ErrorCode.DECLINED, ex.error.code)
        assertEquals(resp.error_id, ex.error.errorId)
        assertEquals("", ex.error.transactionId)
        assertEquals(resp.code, ex.error.supplierErrorCode)
    }

    @Test
    fun `get-transfer - success`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "success",
            data = FWResponseData(
                id = id,
                status = "SUCCESSFUL",
                reference = "ref-00000",
                full_name = "Ray Sponsible",
                account_number = "237670000001",
                amount = 3000.0,
                currency = "XAF",
                fee = 5.0,
                app_fee = 0.0,
                narration = "Sample transfer",
            )
        )
        doReturn(resp).whenever(http).get(any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val response = gateway.getTransfer(id.toString())

        // THEN
        assertEquals(resp.data!!.reference, response.externalId)
        assertEquals(resp.data!!.narration, response.description)
        assertEquals(resp.data!!.amount, response.amount.value)
        assertEquals(resp.data!!.currency, response.amount.currency)
        assertEquals(resp.data!!.full_name, response.payee.fullName)
        assertEquals(resp.data!!.account_number, response.payee.phoneNumber)
        assertEquals(resp.data!!.fee, response.fees.value)
        assertEquals(Status.SUCCESSFUL, response.status)
        assertNull(response.payerMessage)

        val headers = argumentCaptor<Map<String, String>>()
        verify(http).get(
            any(),
            eq("${FWGateway.BASE_URI}/transfers/$id"),
            eq(FWResponse::class.java),
            headers.capture()
        )
        assertEquals("Bearer $secretKey", headers.firstValue["Authorization"])
    }

    @Test
    fun `get-transfer - failure`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "error",
            data = null,
            error_id = "FW-1202910329",
            message = "DECLINED",
            code = "err_declined"
        )
        doReturn(resp).whenever(http).get(any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val ex = assertThrows<PaymentException> {
            gateway.getTransfer(id.toString())
        }

        // THEN
        assertEquals(ErrorCode.DECLINED, ex.error.code)
        assertEquals(resp.error_id, ex.error.errorId)
        assertEquals("", ex.error.transactionId)
        assertEquals(resp.code, ex.error.supplierErrorCode)
    }

    @Test
    fun `payment - successful`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "success",
            data = FWResponseData(
                id = id,
                status = "SUCCESSFUL",
                flw_ref = "323232323",
                app_fee = 10.0,
                fee = 1.0
            )
        )
        doReturn(resp).whenever(http).post(any(), any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val request = createPaymentRequest("237670000001")
        val response = gateway.createPayment(request)

        // THEN
        assertEquals(Status.SUCCESSFUL, response.status)
        assertEquals(id.toString(), response.transactionId)
        assertEquals(resp.data?.flw_ref, response.financialTransactionId)
        assertEquals(resp.data?.app_fee, response.fees.value)

        val headers = argumentCaptor<Map<String, String>>()
        verify(http).post(
            any(),
            eq("${FWGateway.BASE_URI}/charges?type=mobile_money_franco"),
            any(),
            eq(FWResponse::class.java),
            headers.capture()
        )
        assertEquals("Bearer $secretKey", headers.firstValue["Authorization"])
    }

    @Test
    fun `payment - pending`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "success",
            data = FWResponseData(
                id = id,
                status = "NEW",
                flw_ref = "323232323"
            )
        )
        doReturn(resp).whenever(http).post(any(), any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val request = createPaymentRequest("237670000001")
        val response = gateway.createPayment(request)

        // THEN
        assertEquals(Status.PENDING, response.status)
        assertEquals(id.toString(), response.transactionId)
        assertEquals(resp.data?.flw_ref, response.financialTransactionId)

        val headers = argumentCaptor<Map<String, String>>()
        verify(http).post(
            any(),
            eq("${FWGateway.BASE_URI}/charges?type=mobile_money_franco"),
            any(),
            eq(FWResponse::class.java),
            headers.capture()
        )
        assertEquals("Bearer $secretKey", headers.firstValue["Authorization"])
    }

    @Test
    fun `payment - error`() {
        // GIVEN
        val resp = FWResponse(
            status = "error",
            data = null,
            error_id = "FW-1202910329",
            message = "DECLINED",
            code = "err_declined"
        )
        val exception = HttpException(
            statusCode = 500,
            bodyString = ObjectMapper().writeValueAsString(resp)
        )
        doThrow(exception).whenever(http).post(any(), any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val request = createPaymentRequest("237670000001")
        val ex = assertThrows<PaymentException> {
            gateway.createPayment(request)
        }

        // THEN
        assertEquals(ErrorCode.DECLINED, ex.error.code)
        assertEquals(resp.error_id, ex.error.errorId)
        assertEquals("", ex.error.transactionId)
        assertEquals(resp.code, ex.error.supplierErrorCode)
    }

    @Test
    fun `get-payment - successful`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "success",
            data = FWResponseData(
                id = id,
                status = "SUCCESSFUL",
                tx_ref = "tx-0000",
                flw_ref = "flw-00000",
                full_name = "Ray Sponsible",
                account_number = "237670000001",
                amount = 3000.0,
                currency = "XAF",
                fee = 5.0,
                app_fee = 55.0,
                narration = "Sample transfer",
            )
        )
        doReturn(resp).whenever(http).get(any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val response = gateway.getPayment(id.toString())

        // THEN
        assertEquals(resp.data!!.tx_ref, response.externalId)
        assertEquals(resp.data!!.flw_ref, response.financialTransactionId)
        assertEquals(resp.data!!.narration, response.description)
        assertEquals(resp.data!!.amount, response.amount.value)
        assertEquals(resp.data!!.currency, response.amount.currency)
        assertEquals(resp.data!!.full_name, response.payer.fullName)
        assertEquals(resp.data!!.account_number, response.payer.phoneNumber)
        assertEquals(resp.data!!.app_fee, response.fees.value)
        assertEquals(Status.SUCCESSFUL, response.status)
        assertNull(response.payerMessage)

        val headers = argumentCaptor<Map<String, String>>()
        verify(http).get(
            any(),
            eq("${FWGateway.BASE_URI}/transactions/$id/verify"),
            eq(FWResponse::class.java),
            headers.capture()
        )
        assertEquals("Bearer $secretKey", headers.firstValue["Authorization"])
    }

    @Test
    fun `get-payment - failure`() {
        // GIVEN
        val id = System.currentTimeMillis()
        val resp = FWResponse(
            status = "error",
            data = null,
            error_id = "FW-1202910329",
            message = "DECLINED",
            code = "err_declined"
        )
        doReturn(resp).whenever(http).get(any(), any(), eq(FWResponse::class.java), any())

        // WHEN
        val ex = assertThrows<PaymentException> {
            gateway.getPayment(id.toString())
        }

        // THEN
        assertEquals(ErrorCode.DECLINED, ex.error.code)
        assertEquals(resp.error_id, ex.error.errorId)
        assertEquals("", ex.error.transactionId)
        assertEquals(resp.code, ex.error.supplierErrorCode)
    }

    private fun createTransferRequest(phoneNumber: String) = CreateTransferRequest(
        payee = Party(
            fullName = "Ray Sponsible",
            phoneNumber = phoneNumber
        ),
        amount = Money(
            value = 100.0,
            currency = "XAF"
        ),
        payerMessage = "Hello wold",
        externalId = UUID.randomUUID().toString(),
        description = "Sample product"
    )

    private fun createPaymentRequest(phoneNumber: String) = CreatePaymentRequest(
        payer = Party(
            fullName = "Ray Sponsible",
            phoneNumber = phoneNumber,
            email = "ray.sponsible@yahoo.com"
        ),
        amount = Money(
            value = 100.0,
            currency = "XAF"
        ),
        payerMessage = "Hello wold",
        externalId = UUID.randomUUID().toString(),
        description = "Sample product"
    )
}
