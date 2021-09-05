package com.wutsi.platform.payment.core

class HttpException(
    val statusCode: Int,
    val bodyString: String,
    message: String? = null,
    cause: Exception? = null
) : RuntimeException(message, cause) {
    override fun getLocalizedMessage(): String {
        return "$statusCode - $bodyString"
    }
}
