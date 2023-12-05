package no.nav.api.digdir

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.api.digdir.DigdirService.*
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureDigdirRoutes(digdirService: DigdirService) {
    route("digdir/{fnr}/kontaktinformasjon") {
        install(NotarizedRoute()) {
            get = Api.kontaktinformasjon
        }
        get {
            val payload = call.getJWT()
            val ident = requireNotNull(call.parameters["fnr"])
            call.respond(digdirService.hentKontaktinformasjon(ident, payload))
        }
    }
}

private object Api {
    val kontaktinformasjon =
        GetInfo.builder {
            summary("Brukers epost og mobiltelefonnummer")
            description("Hentes fra digdir-proxy")
            request {
                parameters(CommonModels.fnrParameter)
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<Kontaktinformasjon>())
                description("Brukers epost og mobiltelefonnummer")
            }
            tags("Brukers epost og mobiltelefonnummer")
            canRespond(CommonModels.standardResponses)
        }
}
