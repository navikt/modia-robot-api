package no.nav.api.debug

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.utils.DownstreamApi
import no.nav.utils.createMachineToMachineToken
import no.nav.utils.isProd
import kotlin.reflect.typeOf

fun Route.configureDebugRoutes(tokenClient: MachineToMachineTokenClient) {
    route("debug") {
        route("token-exchange") {
            install(NotarizedRoute()) {
                post = Api.post
            }
            post {
                if (isProd()) {
                    call.respond(HttpStatusCode.BadRequest, "Unable to use debug endpoints in production")
                } else {
                    val request: TokenExchangeRequest = call.receive()
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
    val token: String,
)

private object Api {
    val post = PostInfo.builder {
        summary("Hente ut downstream-api token")
        description("")
        request {
            requestType(typeOf<TokenExchangeRequest>())
            description("Beskrivelse av applikasjonen man ønsker token til")
            examples(
                Pair(
                    "example",
                    TokenExchangeRequest(
                        cluster = "dev-fss",
                        namespace = "teamname",
                        application = "app-api",
                    ),
                ),
            )
        }
        response {
            responseCode(HttpStatusCode.OK)
            responseType(typeOf<TokenExchangeResponse>())
            description("Token for bruk mot gitt applikasjon")
        }
        tags("Debug")
        canRespond(CommonModels.standardResponses)
//        canRespond {
//            CommonModels.standardResponses
//        }
    }
}
