package no.nav.api.tps

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme

fun Route.configureTpsRoutes(
    tpsService: TpsService
) {
    route("tps/{fnr}/kontonummer") {
        notarizedGet(Api.kontonummer) {
            val fnr = checkNotNull(call.parameters["fnr"])
            call.respond(tpsService.hentKontonummer(fnr))
        }
    }
}

private object Api {
    val kontonummer = GetInfo<CommonModels.FnrParameter, TpsService.Kontonummer>(
        summary = "Brukers kontonummer",
        description = "Hentes fra TPS",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers kontonummer om det eksisterer i TPS"
        ),
        tags = setOf("TPS"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}