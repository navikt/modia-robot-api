package no.nav.utils

import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient

class DownstreamApi(
    val cluster: String,
    val namespace: String,
    val application: String,
) {
    companion object
}

private fun DownstreamApi.tokenscope(): String = "api://$cluster.$namespace.$application/.default"

fun DownstreamApi.Companion.parse(value: String): DownstreamApi {
    val parts = value.split(":")
    check(parts.size == 3) { "DownstreamApi string must contain 3 parts" }

    val cluster = parts[0]
    val namespace = parts[1]
    val application = parts[2]

    return DownstreamApi(cluster = cluster, namespace = namespace, application = application)
}

fun MachineToMachineTokenClient.createMachineToMachineToken(api: DownstreamApi): String = this.createMachineToMachineToken(api.tokenscope())

interface BoundedMachineToMachineTokenClient {
    fun createMachineToMachineToken(): String
}

interface BoundedOnBehalfOfTokenClient {
    fun exchangeOnBehalfOfToken(accesstoken: String): String
}

fun MachineToMachineTokenClient.bindTo(api: DownstreamApi) =
    object : BoundedMachineToMachineTokenClient {
        override fun createMachineToMachineToken() = createMachineToMachineToken(api.tokenscope())
    }

fun OnBehalfOfTokenClient.bindTo(api: DownstreamApi) =
    object : BoundedOnBehalfOfTokenClient {
        override fun exchangeOnBehalfOfToken(accesstoken: String) = exchangeOnBehalfOfToken(api.tokenscope(), accesstoken)
    }

fun ApplicationCall.getJWT(): String {
    val authHeader = this.request.parseAuthorizationHeader()
    if (authHeader != null && authHeader is HttpAuthHeader.Single && authHeader.authScheme == "Bearer") {
        return authHeader.blob
    }

    throw Exception("Missing authorization header")
}

fun ApplicationCall.getJWTPrincipalSubject() =
    checkNotNull(this.principal<JWTPrincipal>()?.subject) {
        "Could not extract subject from JWT"
    }
