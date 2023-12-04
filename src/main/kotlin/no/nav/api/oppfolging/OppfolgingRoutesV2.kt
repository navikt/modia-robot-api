package no.nav.api.oppfolging

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

fun Route.configureOppfolgingRoutesV2(
    oppfolgingService: OppfolgingService,
) {
    route("oppfolging/veileder") {
        notarizedPost(ApiV2.veileder) {
            val payload = call.getJWT()
            val fnr = requireNotNull(call.receive<String>())
            call.respond(oppfolgingService.hentOppfolging(fnr, payload))
        }
    }
}

private object ApiV2 {
    val veileder = PostInfo<Unit, String, OppfolgingService.Oppfolging>(
        summary = "Brukers oppfølgingsveileder",
        description = "Hentes fra veilarboppfølging",
        requestInfo = RequestInfo(
            description = "Brukers fnr",
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Navn og ident til brukers veileder"
        ),
        tags = setOf("Oppfølging"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses
    )
}
