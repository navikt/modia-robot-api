package no.nav.api.utbetalinger

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import no.nav.api.CommonModels
import no.nav.api.utbetalinger.UtbetalingerService.*
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureUtbetalingerRoutesV2(utbetalingerService: UtbetalingerService) {
    route("v2/utbetalinger/ytelseoversikt") {
        install(NotarizedRoute()) { post = ApiV2.utbetalinger }
        post {
            val payload = call.getJWT()
            val fnr = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val fra = LocalDate.parse(call.request.queryParameters["fra"] ?: "")
            val til = LocalDate.parse(call.request.queryParameters["til"] ?: "")

            call.respond(utbetalingerService.hentUtbetalinger(fnr, fra, til, payload))
        }
    }
}

private object ApiV2 {
    val utbetalinger =
        PostInfo.builder {
            summary("Brukers utbetalinger")
            description("Hentes fra utbetaldata")
            request {
                parameters(Models.fraParam, Models.tilParam)
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<List<Utbetalinger>>())
                description("Brukers utbetalinger")
            }
            tags("Utbetalinger")
            canRespond(CommonModels.standardResponses)
        }
}
