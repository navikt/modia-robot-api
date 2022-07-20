package no.nav.api.utbetalinger

import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme

fun Route.configureUtbetalingerRoutes() {
    route("utbetalinger/{fnr}/ytelseoversikt") {
        notarizedGet(Api.utbetalinger) {
            val fnr = call.parameters["fnr"]
            val fra = LocalDate.parse(call.request.queryParameters["fra"] ?: "")
            val til = LocalDate.parse(call.request.queryParameters["til"] ?: "")

            call.respond(Models.Utbetalinger(
                ytelse = "Dagpenger",
                fra = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                til = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            ))
        }
    }
}

private object Api {
    val utbetalinger = GetInfo<Models.UrlParameters, Models.Utbetalinger>(
        summary = "Brukers utbetalinger",
        description = "Hentes fra utbetaldata",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers utbetalinger"
        ),
        tags = setOf("Utbetalinger"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}

private object Models {
    class UrlParameters(
        fnr: String,
        @Param(type = ParamType.QUERY)
        val fra: LocalDate,
        @Param(type = ParamType.QUERY)
        val til: LocalDate,
    ) : CommonModels.FnrParameter(fnr)

    @Serializable
    data class Utbetalinger(
        val ytelse: String,
        val fra: LocalDate,
        val til: LocalDate
    )
}
