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
import no.nav.utils.getJWTPrincipalPayload

fun Route.configureDigdirRoutes(
    digdirService: DigdirService,
) {
    route("digdir/{fnr}/kontaktinformasjon") {
        notarizedGet(Api.kontaktinformasjon) {
            val payload = call.getJWTPrincipalPayload()
            val ident = requireNotNull(call.parameters["fnr"])
            call.respond(digdirService.hentKontaktinformasjon(ident, payload.token))
        }
    }
}
private object Api {
    val kontaktinformasjon = GetInfo<CommonModels.FnrParameter, Kontaktinformasjon>(
        summary = "Brukers epost og mobiltelefonnummer",
        description = "Hentes fra digdir-proxy",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers epost og mobiltelefonnummer"
        ),
        tags = setOf("Kontakt- og reservasjonsregisteret"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses
    )
}
