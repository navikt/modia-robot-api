package no.nav.api.digdir

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.api.CommonModels
import no.nav.api.digdir.DigdirService.*
import no.nav.plugins.securityScheme

fun Route.configureDigdirRoutes(
    digdirService: DigdirService
) {
    route("digdir/{fnr}/epost") {
        notarizedGet(Api.epost) {
            val ident = requireNotNull(call.parameters["fnr"])
            call.respond(digdirService.hentEpost(ident))
        }
    }
}
private object Api {
    val epost = GetInfo<CommonModels.FnrParameter, Epost>(
        summary = "Brukers epost",
        description = "Hentes fra digdir-proxy",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers epost"
        ),
        tags = setOf("Kontakt- og reservasjonsregisteret"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}
