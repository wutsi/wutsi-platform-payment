package com.wutsi.platform.payment.provider.mtn.product

import com.wutsi.platform.payment.core.Http
import com.wutsi.platform.payment.provider.mtn.Fixtures
import com.wutsi.platform.payment.provider.mtn.model.Party
import com.wutsi.platform.payment.provider.mtn.model.RequestToPayRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class CollectionTest {
    private var accessToken: String = ""

    @BeforeEach
    fun setUp() {
        if (accessToken.isEmpty()) {
            accessToken = createProduct().token().access_token
        }
    }

    @Test
    fun `request to pay and SUCCESSFUL`() {
        val api = createProduct()
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
        val referenceId = UUID.randomUUID().toString()
        val request = createRequestToPayRequest(Fixtures.NUMBER_REJECTED)
        api.requestToPay(referenceId, accessToken, request)

        val response = api.requestToPay(referenceId, accessToken)
        assertEquals("FAILED", response.status)
    }

    @Test
    fun `request to pay and FAILED`() {
        val api = createProduct()
        val referenceId = UUID.randomUUID().toString()
        val request = createRequestToPayRequest(Fixtures.NUMBER_FAILED)
        api.requestToPay(referenceId, accessToken, request)

        val response = api.requestToPay(referenceId, accessToken)
        assertEquals("FAILED", response.status)
    }

    @Test
    fun `request to pay and PENDING`() {
        val api = createProduct()
        val referenceId = UUID.randomUUID().toString()
        val request = createRequestToPayRequest(Fixtures.NUMBER_PENDING)
        api.requestToPay(referenceId, accessToken, request)

        val response = api.requestToPay(referenceId, accessToken)
        assertEquals("PENDING", response.status)
    }

    private fun createProduct(
        config: ProductConfig = Fixtures.createCollectionConfig(),
        http: Http = Fixtures.createHttp()
    ): Collection =
        Collection(config, http)

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
