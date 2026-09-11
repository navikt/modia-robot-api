package no.nav.api.oppfolging

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureOppfolgingRoutes(oppfolgingService: OppfolgingService) {
    route("oppfolging/veileder") {
        install(NotarizedRoute()) {
            post = Api.veileder
        }
        post {
            val payload = call.getJWT()
            val fnr = call.deserializeFnr()
            call.respond(oppfolgingService.hentOppfolging(fnr, payload))
        }
    }
}

private object Api {
    val veileder =
        PostInfo.builder {
            summary("Brukers oppfølgingsveileder")
            description("Hentes fra veilarboppfølging")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseType(typeOf<OppfolgingService.Oppfolging>())
                responseCode(HttpStatusCode.OK)
                description("Navn og ident til brukers veileder")
            }
            tags("Oppfølging")
            canRespond(CommonModels.standardResponses + CommonModels.badRequestResponse)
        }
}
