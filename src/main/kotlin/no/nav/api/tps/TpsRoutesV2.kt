package no.nav.api.tps

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
import no.nav.plugins.securityScheme

fun Route.configureTpsRoutesV2(tpsService: TpsService) {
    route("tps/kontonummer") {
        notarizedPost(ApiV2.kontonummer) {
            val fnr = requireNotNull(call.receive<String>())
            call.respond(tpsService.hentKontonummer(fnr))
        }
    }
}

private object ApiV2 {
    val kontonummer =
        PostInfo<Unit, String, TpsService.Kontonummer>(
            summary = "Brukers kontonummer",
            description = "Hentes fra TPS",
            responseInfo =
                ResponseInfo(
                    status = HttpStatusCode.OK,
                    description = "Brukers kontonummer om det eksisterer i TPS",
                ),
            requestInfo =
                RequestInfo(
                    description = "Brukers fnr",
                ),
            tags = setOf("TPS"),
            securitySchemes = setOf(securityScheme.name),
            canThrow = CommonModels.standardResponses,
        )
}
