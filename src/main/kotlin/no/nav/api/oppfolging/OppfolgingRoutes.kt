package no.nav.api.oppfolging

import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.plugins.securityScheme

@Serializable
data class VeilederNavn(val fornavn: String, val etternavn: String)

fun Route.configureOppfolgingRoutes(
    oppfolgingService: OppfolgingService
) {
    route("api/oppfolging/{fnr}/veileder") {
        notarizedGet(Api.veileder) {
            val fnr = requireNotNull(call.parameters["fnr"])
            call.respond(oppfolgingService.hentOppfolging(fnr))
        }
    }
}

private object Api {
    val veileder = GetInfo<FnrParameter, VeilederNavn>(
        summary = "Henter brukers veileder",
        description = "Henter informasjon fra veilarboppfølging",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Navn til brukers veileder"
        ),
        tags = setOf("Oppfølging"),
        securitySchemes = setOf(securityScheme.name)
    )

    @Serializable
    class FnrParameter(
        @Param(type = ParamType.PATH)
        val fnr: String,
    )
}
