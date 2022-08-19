package no.nav.utils

import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.*
import io.ktor.client.request.*
import no.nav.common.utils.IdUtils
import no.nav.personoversikt.utils.SelftestGenerator
import java.net.URL

typealias HeadersBuilder = HttpRequestBuilder.() -> Unit

class LoggingGraphQLKtorClient(
    private val name: String,
    critical: Boolean,
    url: URL,
    httpClient: HttpClient
) : GraphQLKtorClient(url, httpClient) {
    val selftestReporter = SelftestGenerator.Reporter(name, critical)
        // Report OK to get application ready (/isReady)
        .also { it.reportOk() }

    override suspend fun <T : Any> execute(
        request: GraphQLClientRequest<T>,
        requestCustomizer: HeadersBuilder
    ): GraphQLClientResponse<T> {
        val callId: String = getCallId()
        val requestId = IdUtils.generateId()
        try {
            TjenestekallLogger.info(
                "${name}-request: $callId ($requestId)",
                mapOf(
                    "request" to request
                )
            )
            val response = super.execute(request, requestCustomizer)
            val logMessage = mapOf(
                "data" to response.data,
                "errors" to response.errors,
                "extensions" to response.extensions
            )

            if (response.errors?.isNotEmpty() == true) {
                TjenestekallLogger.error(
                    "$name-response-error: $callId ($requestId)",
                    logMessage
                )
                val exception = Exception(response.errors!!.joinToString(", ") { it.message })
                selftestReporter.reportError(exception)
                throw exception
            }
            TjenestekallLogger.info(
                "$name-response: $callId ($requestId)",
                logMessage
            )
            selftestReporter.reportOk()
            return response
        } catch (exception: Throwable) {
            TjenestekallLogger.error(
                "${name}-response-error: $callId ($requestId)",
                mapOf("exception" to exception)
            )
            selftestReporter.reportError(exception)
            throw exception
        }
    }
}