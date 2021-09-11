package com.wutsi.platform.payment.provider.mtn.product

import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.Fixtures
import com.wutsi.platform.payment.provider.mtn.model.Party
import com.wutsi.platform.payment.provider.mtn.model.TransferRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class DisbursementTest {
    private var accessToken: String = ""

    @BeforeEach
    fun setUp() {
        if (accessToken.isEmpty()) {
            accessToken = createProduct().token().access_token
        }
    }

    @Test
    fun `transfer SUCCESSFUL`() {
        val api = createProduct()
        val referenceId = UUID.randomUUID().toString()
        val request = createRequest(Fixtures.NUMBER_SUCCESS)
        api.transfer(referenceId, accessToken, request)

        val response = api.transfer(referenceId, accessToken)
        assertEquals("SUCCESSFUL", response.status)
        assertEquals(request.amount, response.amount)
        assertEquals(request.currency, response.currency)
        assertEquals(request.externalId, response.externalId)
        assertEquals(request.payerMessage, response.payerMessage)
        assertEquals(request.payee.partyId, response.payee.partyId)
        assertEquals(request.payee.partyIdType, response.payee.partyIdType)
        assertNotNull(response.financialTransactionId)
    }

    @Test
    fun `request to pay and REJECTED`() {
        val api = createProduct()
        val referenceId = UUID.randomUUID().toString()
        val request = createRequest(Fixtures.NUMBER_REJECTED)
        api.transfer(referenceId, accessToken, request)

        val response = api.transfer(referenceId, accessToken)
        assertEquals("FAILED", response.status)
    }

    @Test
    fun `request to pay and FAILED`() {
        val api = createProduct()
        val referenceId = UUID.randomUUID().toString()
        val request = createRequest(Fixtures.NUMBER_FAILED)
        api.transfer(referenceId, accessToken, request)

        val response = api.transfer(referenceId, accessToken)
        assertEquals("FAILED", response.status)
    }

    @Test
    fun `request to pay and PENDING`() {
        val api = createProduct()
        val referenceId = UUID.randomUUID().toString()
        val request = createRequest(Fixtures.NUMBER_PENDING)
        api.transfer(referenceId, accessToken, request)

        val response = api.transfer(referenceId, accessToken)
        assertEquals("PENDING", response.status)
    }

    private fun createProduct(
        config: ProductConfig = Fixtures.createDisbursementConfig(),
        http: Http = Fixtures.createHttp()
    ): Disbursement =
        Disbursement(config, http)

    private fun createRequest(number: String) = TransferRequest(
        payeeNote = "Yo man",
        currency = "EUR",
        amount = "100",
        externalId = "123",
        payerMessage = "Yo man",
        payee = Party(
            partyId = number
        )
    )
}
