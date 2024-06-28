package no.nav.api.kontonummer

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import no.nav.utils.getJWT
import no.nav.utils.getJWTPrincipalSubject
import kotlin.reflect.typeOf

fun Route.configureKontonummerRegisterRoutes(kontonummerRegister: KontonummerRegister) {
    route("kontonummer-register/kontonummer") {
        install(NotarizedRoute()) {
            post = ApiV2.kontonummer
        }
        post {
            val fnr = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val ident = call.getJWTPrincipalSubject()
            val token = call.getJWT()
            call.respond(kontonummerRegister.hentKontonummer(fnr, ident, token))
        }
    }
}

private object ApiV2 {
    val kontonummer =
        PostInfo.builder {
            summary("Brukers kontonummer")
            description("Hentes fra kontonummer register")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseType(typeOf<KontonummerRegister.Kontonummer>())
                responseCode(HttpStatusCode.OK)
                description("Brukers kontonummer om det eksisterer i kontonummer register")
            }
            tags("KontonummerRegister")
            canRespond(CommonModels.standardResponses)
        }
}
