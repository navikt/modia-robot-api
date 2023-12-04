package no.nav.api.pdl

import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme
import no.nav.utils.getJWT

fun Route.configurePdlRoutesV2(pdlService: PdlService) {
    route("pdl") {
        notarizedPost(ApiV2.personalia) {
            val payload = call.getJWT()
            val fnr = requireNotNull(call.receive<String>())
            call.respond(pdlService.hentPersonalia(fnr, payload))
        }
    }
}
private object ApiV2 {
    val personalia = PostInfo<Unit, String, PdlPersonalia>(
        summary = "Generelle personopplysninger",
        description = "Hentes fra PDL",
        requestInfo = RequestInfo(
            description = "Brukers fnr",
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers pdl data",
        ),
        tags = setOf("PDL"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}
