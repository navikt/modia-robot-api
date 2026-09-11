package no.nav.api.utbetalinger

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import no.nav.api.CommonModels
import no.nav.api.utbetalinger.UtbetalingerService.*
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import no.nav.plugins.HttpErrorResponse
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureUtbetalingerRoutes(utbetalingerService: UtbetalingerService) {
    route("utbetalinger/ytelseoversikt") {
        install(NotarizedRoute()) { post = ApiV2.utbetalinger }
        post {
            val payload = call.getJWT()
            val fnr = call.deserializeFnr()
            val fra = call.parseDato("fra") ?: return@post call.respondUgyldigDato("fra")
            val til = call.parseDato("til") ?: return@post call.respondUgyldigDato("til")

            call.respond(utbetalingerService.hentUtbetalinger(fnr, fra, til, payload))
        }
    }
}

private fun ApplicationCall.parseDato(navn: String): LocalDate? =
    request.queryParameters[navn]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

private suspend fun ApplicationCall.respondUgyldigDato(navn: String) =
    respond(
        HttpStatusCode.BadRequest,
        HttpErrorResponse(
            message = "Spørringsparameteren «$navn» mangler eller er ikke en dato på formatet ÅÅÅÅ-MM-DD",
        ),
    )

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
            canRespond(CommonModels.standardResponses + CommonModels.badRequestResponse)
        }
}

internal object Models {
    private val datoSkjema = TypeDefinition(type = "string", format = "date")

    val fraParam =
        Parameter(
            name = "fra",
            `in` = Parameter.Location.query,
            schema = datoSkjema,
            description = "Første dag i perioden det hentes utbetalinger for, på formatet ÅÅÅÅ-MM-DD",
        )
    val tilParam =
        Parameter(
            name = "til",
            `in` = Parameter.Location.query,
            schema = datoSkjema,
            description = "Siste dag i perioden det hentes utbetalinger for, på formatet ÅÅÅÅ-MM-DD",
        )
}
