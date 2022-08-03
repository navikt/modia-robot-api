package no.nav.api.debug

import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.plugins.securityScheme
import no.nav.utils.DownstreamApi
import no.nav.utils.createMachineToMachineToken
import no.nav.utils.isProd

fun Route.configureDebugRoutes(tokenClient: MachineToMachineTokenClient) {
    route("debug") {
        route("token-exchange") {
            notarizedPost(Api.tokenExhange) {
                if (isProd()) {
                    call.respond(HttpStatusCode.BadRequest, "Unable to use debug endpoints in production")
                } else {
                    val request : TokenExchangeRequest = call.receive()
                    val downstreamApi = DownstreamApi(
                        cluster = request.cluster,
                        namespace = request.namespace,
                        application = request.application,
                    )
                    call.respond(tokenClient.createMachineToMachineToken(downstreamApi))
                }
            }
        }
    }
}

@Serializable
data class TokenExchangeRequest(
    val cluster: String,
    val namespace: String,
    val application: String,
)

@Serializable
data class TokenExchangeResponse(
    val token: String
)

private object Api {
    val tokenExhange = PostInfo<Unit, TokenExchangeRequest, TokenExchangeResponse>(
        summary = "Hente ut downstream-api token",
        description = "",
        requestInfo = RequestInfo(
            description = "Beskrivelse av applikasjonen man ønsker token til",
            examples = mapOf(
                "example" to TokenExchangeRequest(
                    cluster = "dev-fss",
                    namespace = "teamname",
                    application = "app-api"
                )
            )
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Token for bruk mot gitt applikasjon"
        ),
        tags = setOf("Debug"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}