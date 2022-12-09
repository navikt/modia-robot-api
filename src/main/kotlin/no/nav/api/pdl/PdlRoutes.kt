package no.nav.api.pdl

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme
import no.nav.utils.getJWTPrincipalPayload

fun Route.configurePdlRoutes(pdlService: PdlService) {
    route("pdl/{fnr}") {
        notarizedGet(Api.personalia) {
            val payload = call.getJWTPrincipalPayload()
            val fnr = requireNotNull(call.parameters["fnr"])
            call.respond(pdlService.hentPersonalia(fnr, payload.token))
        }
    }
}
private object Api {
    val personalia = GetInfo<CommonModels.FnrParameter, PdlPersonalia>(
        summary = "Generelle personopplysninger",
        description = "Hentes fra PDL",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers pdl data"
        ),
        tags = setOf("PDL"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses
    )
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
        linje3?.filterNotNull()?.joinToString(" ")
    )
}
