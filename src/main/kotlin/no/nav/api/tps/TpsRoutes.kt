package no.nav.api.tps

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import kotlin.reflect.typeOf

fun Route.configureTpsRoutes(tpsService: TpsService) {
    route("tps/kontonummer") {
        install(NotarizedRoute()) {
            post = ApiV2.kontonummer
        }
        post {
            val fnr = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(tpsService.hentKontonummer(fnr))
        }
    }
}

private object ApiV2 {
    val kontonummer =
        PostInfo.builder {
            summary("Brukers kontonummer")
            description("Hentes fra TPS")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseType(typeOf<TpsService.Kontonummer>())
                responseCode(HttpStatusCode.OK)
                description("Brukers kontonummer om det eksisterer i TPS")
            }
            tags("TPS")
            canRespond(CommonModels.standardResponses)
        }
}
