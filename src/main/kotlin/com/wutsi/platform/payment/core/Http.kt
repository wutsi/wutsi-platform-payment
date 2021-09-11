package com.wutsi.platform.payment.core

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpRequest.Builder
import java.net.http.HttpResponse

class Http(
    private val client: HttpClient,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Http::class.java)
    }

    fun <T> get(
        uri: String,
        responseType: Class<T>,
        headers: Map<String, String?> = emptyMap()
    ): T? {
        val request = HttpRequest.newBuilder()
            .uri(URI(uri))
            .headers(headers)
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        LOGGER.debug("GET $uri - ${response.statusCode()}")
        if (response.statusCode() / 100 == 2)
            return if (response.body().isEmpty())
                null
            else
                objectMapper.readValue(response.body(), responseType)
        else
            throw HttpException(response.statusCode(), response.body())
    }

    fun <T> post(
        uri: String,
        requestPayload: Any,
        responseType: Class<T>,
        headers: Map<String, String?> = emptyMap()
    ): T? {
        val request = HttpRequest.newBuilder()
            .uri(URI(uri))
            .headers(headers)
            .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(requestPayload)))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        LOGGER.debug("POST $uri - ${response.statusCode()}")
        if (response.statusCode() / 100 == 2)
            return if (response.body().isEmpty())
                null
            else
                objectMapper.readValue(response.body(), responseType)
        else
            throw HttpException(response.statusCode(), response.body())
    }

    private fun Builder.headers(headers: Map<String, String?>): Builder {
        headers.keys.forEach {
            if (headers[it] != null)
                this.header(it, headers[it])
        }
        return this
    }
}
