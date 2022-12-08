package no.nav.api.oppfolging

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme
import no.nav.utils.getJWTPrincipalPayload

fun Route.configureOppfolgingRoutes(
    oppfolgingService: OppfolgingService,
) {
    route("oppfolging/{fnr}/veileder") {
        notarizedGet(Api.veileder) {
            val payload = call.getJWTPrincipalPayload()
            val fnr = requireNotNull(call.parameters["fnr"])
            call.respond(oppfolgingService.hentOppfolging(fnr, payload.token))
        }
    }
}

private object Api {
    val veileder = GetInfo<CommonModels.FnrParameter, OppfolgingService.Oppfolging>(
        summary = "Brukers oppfølgingsveileder",
        description = "Hentes fra veilarboppfølging",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Navn og ident til brukers veileder"
        ),
        tags = setOf("Oppfølging"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses
    )
}
