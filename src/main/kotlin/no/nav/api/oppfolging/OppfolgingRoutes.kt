package no.nav.api.oppfolging

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureOppfolgingRoutes(oppfolgingService: OppfolgingService) {
    route("oppfolging/{fnr}/veileder") {
        install(NotarizedRoute()) {
            get = Api.veileder
        }
        get {
            val payload = call.getJWT()
            val fnr = requireNotNull(call.parameters["fnr"])
            call.respond(oppfolgingService.hentOppfolging(fnr, payload))
        }
    }
}

private object Api {
    val veileder =
        GetInfo.builder {
            summary("Brukers oppfølgingsveileder")
            description("Hentes fra veilarboppfølging")
            request {
                parameters(CommonModels.fnrParameter)
            }
            response {
                responseType(typeOf<OppfolgingService.Oppfolging>())
                responseCode(HttpStatusCode.OK)
                description("Navn og ident til brukers veileder")
            }
            tags("Oppfølging")
            canRespond(CommonModels.standardResponses)
        }
}
