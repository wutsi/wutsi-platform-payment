package com.wutsi.platform.payment.provider.flutterwave

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreateTransferRequest
import com.wutsi.platform.payment.model.Party
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.http.HttpClient
import java.util.UUID
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class FWGatewayIntegrationTest {
    private val secretKey = "FLWSECK_TEST-b4cb2c97ac5127c3bd06995c0ce1032a-X"
    private lateinit var http: Http
    private lateinit var gateway: Gateway

    @BeforeEach
    fun setUp() {
        val om = ObjectMapper()
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        http = Http(
            HttpClient.newHttpClient(),
            om
        )
        gateway = FWGateway(http, secretKey)
    }

    @Test
    @Ignore
    fun `MobileMoney - CM - payment - success`() {
        // WHEN
        val request = createPaymentRequest("+23700000020")
        val response = gateway.createPayment(request)

        // THEN
        assertEquals(Status.SUCCESSFUL, response.status)
    }

    @Test
    fun `MobileMoney - CM - transfer`() {
        // TRANSFER
        println("Transfering...")
        val request = createTransferRequest("+23700000020")
        val response = gateway.createTransfer(request)
        assertEquals(Status.PENDING, response.status)
        assertEquals(500.0, response.fees.value)
        assertNotNull(response.transactionId)
        assertNull(response.financialTransactionId)

        println("Fetching details...")
        val response2 = gateway.getTransfer(response.transactionId)
        assertEquals(request.amount, response2.amount)
        assertEquals(response.fees, response2.fees)
        assertEquals(response.status, response2.status)
        assertEquals(request.externalId, response2.externalId)
        assertNull(response2.payee.email)
        assertEquals(request.payee.fullName, response2.payee.fullName)
        assertNull(request.payee.country, response2.payee.country)
        assertEquals(request.payee.phoneNumber.substring(1), response2.payee.phoneNumber)
    }

    private fun createTransferRequest(phoneNumber: String) = CreateTransferRequest(
        payee = Party(
            fullName = "Ray Sponsible",
            phoneNumber = phoneNumber,
            email = ""
        ),
        amount = Money(
            value = 25000.0,
            currency = "XAF"
        ),
        payerMessage = "Hello wold",
        externalId = UUID.randomUUID().toString(),
        description = "Sample product",
    )

    private fun createPaymentRequest(phoneNumber: String) = CreatePaymentRequest(
        payer = Party(
            fullName = "Ray Sponsible",
            phoneNumber = phoneNumber,
            email = "ray.sponsible@yahoo.com",
            country = "CM"
        ),
        amount = Money(
            value = 15000.0,
            currency = "XAF"
        ),
        payerMessage = "Hello wold",
        externalId = UUID.randomUUID().toString(),
        description = "Sample product"
    )
}
