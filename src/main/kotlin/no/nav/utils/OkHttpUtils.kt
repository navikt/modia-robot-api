package no.nav.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.Buffer
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.*

class LoggingInterceptor(
    val name: String,
    val config: Config = DEFAULT_CONFIG,
    val callIdExtractor: (Request) -> String,
) : Interceptor {
    data class Config(
        val ignoreRequestBody: Boolean = false,
        val ignoreResponseBody: Boolean = false,
    )

    companion object {
        @JvmField
        val DEFAULT_CONFIG = Config()
    }

    private val log = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    private fun Request.peekContent(config: Config): String? {
        if (config.ignoreRequestBody) return "IGNORED"
        val copy = this.newBuilder().build()
        val buffer = Buffer()
        copy.body?.writeTo(buffer)

        return buffer.readUtf8()
    }

    private fun Response.peekContent(config: Config): String? {
        if (config.ignoreResponseBody) return "IGNORED"
        return when {
            this.header("Content-Length") == "0" -> "Content-Length: 0, didn't try to peek at body"
            this.code == 204 -> "StatusCode: 204, didn't try to peek at body"
            else -> this.peekBody(Long.MAX_VALUE).string()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val callId = callIdExtractor(request)
        val requestId = UUID.randomUUID().toString()
        val requestBody = request.peekContent(config)

        TjenestekallLogger.info(
            "$name-request: $callId ($requestId)",
            mapOf(
                "url" to request.url.toString(),
                "headers" to request.headers.names().joinToString(", "),
                "body" to requestBody,
            ),
        )

        val timer: Long = System.currentTimeMillis()
        val response: Response =
            runCatching { chain.proceed(request) }
                .onFailure { exception ->
                    log.error("$name-response-error (ID: $callId / $requestId)", exception)
                    TjenestekallLogger.error(
                        "$name-response-error: $callId ($requestId))",
                        mapOf(
                            "exception" to exception,
                            "time" to timer.measure(),
                        ),
                    )
                }.getOrThrow()

        val responseBody = response.peekContent(config)

        if (response.code in 200..299) {
            TjenestekallLogger.info(
                "$name-response: $callId ($requestId)",
                mapOf(
                    "status" to "${response.code} ${response.message}",
                    "time" to timer.measure(),
                    "body" to responseBody,
                ),
            )
        } else {
            TjenestekallLogger.error(
                "$name-response-error: $callId ($requestId)",
                mapOf(
                    "status" to "${response.code} ${response.message}",
                    "time" to timer.measure(),
                    "body" to responseBody,
                ),
            )
        }
        return response
    }
}

private fun Long.measure(): Long = System.currentTimeMillis() - this

open class HeadersInterceptor(
    val headersProvider: () -> Map<String, String>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder =
            chain
                .request()
                .newBuilder()
        headersProvider()
            .forEach { (name, value) -> builder.addHeader(name, value) }

        return chain.proceed(builder.build())
    }
}

class XCorrelationIdInterceptor :
    HeadersInterceptor({
        mapOf("X-Correlation-ID" to getCallId())
    })

class AuthorizationInterceptor(
    val tokenProvider: () -> String,
) : HeadersInterceptor({
        mapOf("Authorization" to "Bearer ${tokenProvider()}")
    })

class BasicAuthorizationInterceptor(
    private val username: String,
    private val password: String,
) : HeadersInterceptor({
        mapOf("Authorization" to Credentials.basic(username, password))
    })

fun getCallId(): String = MDC.get("CallId") ?: UUID.randomUUID().toString()

val navConsumerId = "srvmodiarobotapi"

fun <T : HttpClientEngineConfig> HttpClientConfig<T>.installContentNegotiationAndIgnoreUnknownKeys() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            },
        )
    }
}
