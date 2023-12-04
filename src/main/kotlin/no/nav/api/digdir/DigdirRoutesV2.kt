package no.nav.api.digdir

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
import no.nav.api.digdir.DigdirService.*
import no.nav.plugins.securityScheme
import no.nav.utils.getJWT

fun Route.configureDigdirRoutesV2(
    digdirService: DigdirService,
) {
    route("digdir/kontaktinformasjon") {
        notarizedPost(ApiV2.kontaktinformasjon) {
            val payload = call.getJWT()
            val ident = requireNotNull(call.receive<String>())
            call.respond(digdirService.hentKontaktinformasjon(ident, payload))
        }
    }
}
private object ApiV2 {
    val kontaktinformasjon = PostInfo<Unit, String, Kontaktinformasjon>(
        summary = "Brukers epost og mobiltelefonnummer",
        description = "Hentes fra digdir-proxy",
        requestInfo = RequestInfo(
            description = "Brukers fnr",
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers epost og mobiltelefonnummer"
        ),
        tags = setOf("Kontakt- og reservasjonsregisteret"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses
    )
}
