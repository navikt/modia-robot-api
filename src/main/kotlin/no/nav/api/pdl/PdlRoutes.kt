package no.nav.api.pdl

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.models.FnrRequest
import no.nav.models.deserializeFnr
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configurePdlRoutes(pdlService: PdlService) {
    route("pdl") {
        install(NotarizedRoute()) { post = ApiV2.personalia }
        post {
            val payload = call.getJWT()
            val fnr = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(pdlService.hentPersonalia(fnr, payload))
        }
        route("aktorid") {
            install(NotarizedRoute()) {
                post = ApiV2.hentAktorId
            }
            post {
                val token = call.getJWT()
                val fnr = call.deserializeFnr() ?: return@post call.respond(HttpStatusCode.BadRequest)
                call.respond(AktorIdResponse(pdlService.hentAktoridNullable(fnr, token)))
            }
        }
    }
}

private object ApiV2 {
    val personalia =
        PostInfo.builder {
            summary("Generelle personopplysninger")
            description("Hentes fra PDL")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<PdlPersonalia>())
                description("Brukers pdl data")
            }
            tags("PDL")
            canRespond(CommonModels.standardResponses)
        }

    val hentAktorId =
        PostInfo.builder {
            summary("Hent aktor ID")
            description("Henter aktorid for en person")
            request {
                requestType(typeOf<FnrRequest>())
                description("Brukers ident")
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<AktorIdResponse>())
                description("identens tilhørende aktorid")
            }
            tags("PDL")
            canRespond(CommonModels.standardResponses)
        }
}

@Serializable
data class AktorIdResponse(
    val aktorid: String?,
)

@Serializable
data class PdlPersonalia(
    val alder: Int? = null,
    val bostedsAdresse: PdlAdresse? = null,
    val kontaktAdresse: PdlAdresse? = null,
    val oppholdsAdresse: PdlAdresse? = null,
)

@Serializable
data class PdlAdresse(
    val linje1: String,
    val linje2: String? = null,
    val linje3: String? = null,
) {
    constructor(
        linje1: List<String?>,
        linje2: List<String?>? = null,
        linje3: List<String?>? = null,
    ) : this(
        linje1.filterNotNull().joinToString(" "),
        linje2?.filterNotNull()?.joinToString(" "),
        linje3?.filterNotNull()?.joinToString(" "),
    )
}
