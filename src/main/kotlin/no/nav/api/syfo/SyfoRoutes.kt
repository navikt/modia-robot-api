package no.nav.api.syfo

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureSyfoRoutes(syfoService: SyfoService) {
    route("syfo/veileder") {
        install(NotarizedRoute()) {
            post = ApiV1.veileder
        }
        post {
            val payload = call.getJWT()
            val fnr = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val veileder = syfoService.hentVeileder(fnr, payload)
            call.respond(veileder ?: HttpStatusCode.NoContent)
        }
    }
}

private object ApiV1 {
    val veileder =
        PostInfo.builder {
            summary("Brukers sykefraværsoppfølgingveileder")
            description("Hentes fra isyfo")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseType(typeOf<SyfoService.Veileder>())
                responseCode(HttpStatusCode.OK)
                description("Navn og ident til brukers veileder dersom bruker er tildelt veileder")
            }
            tags("Syfo")
            canRespond(CommonModels.standardResponses)
        }
}
