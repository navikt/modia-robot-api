package no.nav.api.pdl

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

fun Route.configurePdlRoutesV2(pdlService: PdlService) {
    route("pdl") {
        install(NotarizedRoute()) { post = ApiV2.personalia }
        post {
            val payload = call.getJWT()
            val fnr = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(pdlService.hentPersonalia(fnr, payload))
        }
    }
}

private object ApiV2 {
    val personalia =
        PostInfo.builder {
            summary("Generelle personopplysninger")
            description("Hentes fra PDL")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<PdlPersonalia>())
                description("Brukers pdl data")
            }
            tags("PDL")
            canRespond(CommonModels.standardResponses)
        }
}
