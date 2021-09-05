package com.wutsi.platform.payment.provider.mtn

import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.PaymentMethodProvider
import com.wutsi.platform.payment.PaymentMethodType
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.Party
import com.wutsi.platform.payment.model.PayRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse

internal class MTNGatewayTest {
    val gateway: Gateway = createGateway()

    @Test
    fun type() {
        assertEquals(PaymentMethodType.PAYMENT_METHOD_TYPE_MOBILE_PAYMENT, gateway.type())
    }

    @Test
    fun provider() {
        assertEquals(PaymentMethodProvider.PAYMENT_METHOD_PROVIDER_MTN, gateway.provider())
    }

    @Test
    fun pay() {
        val request = createRequest(Fixtures.NUMBER_SUCCESS)
        val response = gateway.pay(request)

        assertNotNull(response.transactionId)
        assertEquals(Status.STATUS_SUCCESS, response.status)
    }

    @Test
    fun payment() {
        val request = createRequest(Fixtures.NUMBER_PENDING)
        val resp = gateway.pay(request)
        val response = gateway.payment(resp.transactionId)

        assertEquals(Status.STATUS_PENDING, response.status)
        assertEquals(request.amount.value, response.amount.value)
        assertEquals(request.amount.currency, response.amount.currency)
        assertEquals(request.payerMessage, response.payerMessage)
        assertEquals(request.description, response.description)
        assertEquals(request.invoiceId, response.invoiceId)
    }

    @Test
    fun failure() {
        val request = createRequest(Fixtures.NUMBER_FAILED)
        val ex = assertThrows<PaymentException> {
            gateway.pay(request)
        }

        assertFalse(ex.error.transactionId.isNullOrBlank())
        assertEquals(ErrorCode.INTERNAL_PROCESSING_ERROR, ex.error.code)
        assertEquals("INTERNAL_PROCESSING_ERROR", ex.error.supplierErrorCode)
    }

    @Test
    fun timeout() {
        val request = createRequest(Fixtures.NUMBER_TIMEOUT)
        val ex = assertThrows<PaymentException> {
            gateway.pay(request)
        }
        val ex2 = assertThrows<PaymentException> {
            gateway.payment(ex.error.transactionId)
        }

        assertEquals(ex.error.transactionId, ex2.error.transactionId)
        assertEquals(ErrorCode.EXPIRED, ex.error.code)
        assertEquals("EXPIRED", ex.error.supplierErrorCode)
    }

    private fun createGateway() =
        MTNGateway(
            collectionConfig = Fixtures.createCollectionApiConfig(),
            http = Fixtures.createHttp()
        )

    private fun createRequest(phoneNumber: String) = PayRequest(
        payer = Party(
            fullName = "Ray Sponsible",
            phoneNumber = phoneNumber
        ),
        amount = Money(
            value = 100.0,
            currency = "EUR"
        ),
        payerMessage = "Hello wold",
        invoiceId = "1111",
        description = "Sample product"
    )
}
