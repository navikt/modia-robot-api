package no.nav.api.utbetalinger

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import no.nav.api.CommonModels
import no.nav.api.utbetalinger.UtbetalingerService.*
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configureUtbetalingerRoutes(
    utbetalingerService: UtbetalingerService,
) {
    route("utbetalinger/{fnr}/ytelseoversikt") {
        install(NotarizedRoute()) { get = Api.utbetalinger }
        get {
            val payload = call.getJWT()
            val fnr = requireNotNull(call.parameters["fnr"])
            val fra = LocalDate.parse(call.request.queryParameters["fra"] ?: "")
            val til = LocalDate.parse(call.request.queryParameters["til"] ?: "")

            call.respond(utbetalingerService.hentUtbetalinger(fnr, fra, til, payload))
        }
    }
}

private object Api {
    val utbetalinger = GetInfo.builder {
        summary("Brukers utbetalinger")
        description("Hentes fra utbetaldata")
        request { parameters(CommonModels.fnrParameter, Models.fraParam, Models.tilParam) }
        response {
            responseCode(HttpStatusCode.OK)
            responseType(typeOf<List<Utbetalinger>>())
            description("Brukers utbetalinger")
        }
        tags("Utbetalinger")
        canRespond(CommonModels.standardResponses)
    }
}

private object Models {
    val fraParam = Parameter(
        name = "fra",
        `in` = Parameter.Location.query,
        schema = TypeDefinition.STRING,
    )
    val tilParam = Parameter(
        name = "til",
        `in` = Parameter.Location.query,
        schema = TypeDefinition.STRING,
    )
}
