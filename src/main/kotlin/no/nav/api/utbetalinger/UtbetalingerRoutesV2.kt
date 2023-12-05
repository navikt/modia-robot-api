package no.nav.api.utbetalinger

import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.datetime.LocalDate
import no.nav.api.CommonModels
import no.nav.api.utbetalinger.UtbetalingerService.*
import no.nav.plugins.securityScheme
import no.nav.utils.getJWT

fun Route.configureUtbetalingerRoutesV2(utbetalingerService: UtbetalingerService) {
    route("utbetalinger/ytelseoversikt") {
        notarizedPost(ApiV2.utbetalinger) {
            val payload = call.getJWT()
            val fnr = requireNotNull(call.receive<String>())
            val fra = LocalDate.parse(call.request.queryParameters["fra"] ?: "")
            val til = LocalDate.parse(call.request.queryParameters["til"] ?: "")

            call.respond(utbetalingerService.hentUtbetalinger(fnr, fra, til, payload))
        }
    }
}

private object ApiV2 {
    val utbetalinger =
        PostInfo<Unit, String, List<Utbetalinger>>(
            summary = "Brukers utbetalinger",
            description = "Hentes fra utbetaldata",
            requestInfo =
                RequestInfo(
                    description = "Brukers fnr",
                ),
            responseInfo =
                ResponseInfo(
                    status = HttpStatusCode.OK,
                    description = "Brukers utbetalinger",
                ),
            tags = setOf("Utbetalinger"),
            securitySchemes = setOf(securityScheme.name),
            canThrow = CommonModels.standardResponses,
        )
}
