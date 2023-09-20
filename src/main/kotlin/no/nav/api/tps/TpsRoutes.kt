package no.nav.api.tps

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import kotlin.reflect.typeOf

fun Route.configureTpsRoutes(
    tpsService: TpsService,
) {
    route("tps/{fnr}/kontonummer") {
        install(NotarizedRoute()) {
            get = Api.kontonummer
        }
        get {
            val fnr = checkNotNull(call.parameters["fnr"])
            call.respond(tpsService.hentKontonummer(fnr))
        }
    }
}

private object Api {
    val kontonummer = GetInfo.builder {
        summary("Brukers kontonummer")
        description("Hentes fra TPS")
        request { parameters(CommonModels.fnrParameter) }
        response {
            responseType(typeOf<TpsService.Kontonummer>())
            responseCode(HttpStatusCode.OK)
            description("Brukers kontonummer om det eksisterer i TPS")
        }
        tags("TPS")
        canRespond(CommonModels.standardResponses)
    }
}
