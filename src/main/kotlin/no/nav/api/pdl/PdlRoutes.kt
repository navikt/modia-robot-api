package no.nav.api.pdl

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.utils.getJWT
import kotlin.reflect.typeOf

fun Route.configurePdlRoutes(pdlService: PdlService) {
    route("pdl/{fnr}") {
        install(NotarizedRoute()) { get = Api.personalia }
        get {
            val payload = call.getJWT()
            val fnr = requireNotNull(call.parameters["fnr"])
            call.respond(pdlService.hentPersonalia(fnr, payload))
        }
    }
}

private object Api {
    val personalia =
        GetInfo.builder {
            summary("Generelle personopplysninger")
            description("Hentes fra PDL")
            request { parameters(CommonModels.fnrParameter) }
            response {
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<PdlPersonalia>())
                description("Brukers pdl data")
            }
            tags("PDL")
            canRespond(CommonModels.standardResponses)
        }
}

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
