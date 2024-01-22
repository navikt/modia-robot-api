package no.nav.api.digdir

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.api.digdir.DigdirService.*
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureDigdirRoutes(digdirService: DigdirService) {
    route("digdir/kontaktinformasjon") {
        install(NotarizedRoute()) {
            post = ApiV2.kontaktinformasjon
        }
        post {
            val payload = call.getJWT()
            val ident = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(digdirService.hentKontaktinformasjon(ident, payload))
        }
    }
}

private object ApiV2 {
    val kontaktinformasjon =
        PostInfo.builder {
            summary("Brukers epost og mobiltelefonnummer")
            description("Hentes fra digdir-proxy")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
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
