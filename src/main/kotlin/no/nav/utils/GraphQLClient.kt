package no.nav.utils

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.reflect.KClass

interface GraphQLVariables
interface GraphQLResult
@Serializable
data class GraphQLError(
    val message: String
)
@Serializable
data class GraphQLResponse<DATA>(
    val data: DATA? = null,
    val errors: List<GraphQLError>? = null,
)
interface GraphQLRequest<VARS : GraphQLVariables, TYPE : GraphQLResult> {
    val query: String
    val variables: VARS
}
data class GraphQLClientConfig(
    val serviceName: String,
    val requestConfig: suspend HttpRequestBuilder.(callId: String) -> Unit = {},
)
class GraphQLClient(
    val config: GraphQLClientConfig,
    val httpClient: HttpClient
) {
    suspend inline fun <VARS : GraphQLVariables, reified DATA : GraphQLResult, REQ : GraphQLRequest<VARS, DATA>> execute(
        request: REQ
    ): GraphQLResponse<DATA> {
        val callId: String = getCallId()
        val requestId: String = UUID.randomUUID().toString()
        try {
            TjenestekallLogger.info(
                "${config.serviceName}-request: $callId ($requestId)",
                mapOf(
                    "request" to request
                )
            )

            val httpResponse = httpClient.request<HttpResponse> {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                config.requestConfig.invoke(this, callId)
                body = request
            }
            val response = httpResponse.call.receive<GraphQLResponse<DATA>>()
            val logMessage = mapOf(
                "status" to httpResponse.status,
                "data" to response.data,
                "errors" to response.errors,
            )
            if (response.errors?.isNotEmpty() == true) {
                TjenestekallLogger.error(
                    "${config.serviceName}-response-error: $callId ($requestId)",
                    logMessage
                )
                throw Exception(response.errors.joinToString(", ") { it.message })
            }
            TjenestekallLogger.info(
                "${config.serviceName}-response: $callId ($requestId)",
                logMessage
            )
            return response
        } catch (exception: Throwable) {
            TjenestekallLogger.error(
                "${config.serviceName}-response-error: $callId ($requestId)",
                mapOf("exception" to exception)
            )
            throw exception
        }
    }

    companion object {
        fun readQuery(source: String, name: String): String {
            return GraphQLClient::class.java
                .getResource("/$source/$name.graphql")
                ?.readText()
                ?.replace("[\n\r]", "")
                ?: throw Exception("Unknown graphql file: \"/$source/$name.graphql\"")
        }
    }
}