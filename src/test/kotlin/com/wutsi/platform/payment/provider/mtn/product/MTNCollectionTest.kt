package com.wutsi.platform.payment.provider.mtn.product

import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.Fixtures
import com.wutsi.platform.payment.provider.mtn.MTNApiConfig
import com.wutsi.platform.payment.provider.mtn.model.Party
import com.wutsi.platform.payment.provider.mtn.model.RequestToPayRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class MTNCollectionTest {
    @Test
    fun token() {
        val api = createProduct()
        val response = api.token()

        println(">>> access_token: ${response.access_token}")
        assertNotNull(response.access_token)
        assertNotNull(response.token_type)
        assertNotNull(response.expires_in)
    }

    @Test
    fun `request to pay and SUCCESSFUL`() {
        val api = createProduct()
        val accessToken = api.token().access_token
        val referenceId = UUID.randomUUID().toString()
        val request = createRequestToPayRequest("237221234100")
        api.requestToPay(referenceId, accessToken, request)

        val response = api.requestToPay(referenceId, accessToken)
        assertEquals("SUCCESSFUL", response.status)
        assertEquals(request.amount, response.amount)
        assertEquals(request.currency, response.currency)
        assertEquals(request.externalId, response.externalId)
        assertEquals(request.payerMessage, response.payerMessage)
        assertEquals(request.payer.partyId, response.payer.partyId)
        assertEquals(request.payer.partyIdType, response.payer.partyIdType)
        assertNotNull(response.financialTransactionId)
    }

    @Test
    fun `request to pay and REJECTED`() {
        val api = createProduct()
        val accessToken = api.token().access_token
        val referenceId = UUID.randomUUID().toString()
        val request = createRequestToPayRequest(Fixtures.NUMBER_REJECTED)
        api.requestToPay(referenceId, accessToken, request)

        val response = api.requestToPay(referenceId, accessToken)
        assertEquals("FAILED", response.status)
    }

    @Test
    fun `request to pay and FAILED`() {
        val api = createProduct()
        val accessToken = api.token().access_token
        val referenceId = UUID.randomUUID().toString()
        val request = createRequestToPayRequest(Fixtures.NUMBER_FAILED)
        api.requestToPay(referenceId, accessToken, request)

        val response = api.requestToPay(referenceId, accessToken)
        assertEquals("FAILED", response.status)
    }

    @Test
    fun `request to pay and PENDING`() {
        val api = createProduct()
        val accessToken = api.token().access_token
        val referenceId = UUID.randomUUID().toString()
        val request = createRequestToPayRequest(Fixtures.NUMBER_PENDING)
        api.requestToPay(referenceId, accessToken, request)

        val response = api.requestToPay(referenceId, accessToken)
        assertEquals("PENDING", response.status)
    }

    private fun createProduct(
        config: MTNApiConfig = Fixtures.createCollectionApiConfig(),
        http: Http = Fixtures.createHttp()
    ): MTNCollection =
        MTNCollection(config, http)

    private fun createRequestToPayRequest(number: String) = RequestToPayRequest(
        payeeNote = "Yo man",
        currency = "EUR",
        amount = "100",
        externalId = "123",
        payerMessage = "Yo man",
        payer = Party(
            partyId = number
        )
    )
}
