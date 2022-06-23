package no.nav.api.pdl

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.http.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme

fun Route.configurePdlRoutes() {
    route("pdl/{fnr}") {
        notarizedGet(Api.personalia) {
            TODO()
        }
    }
}
private object Api {
    val personalia = GetInfo<CommonModels.FnrParameter, Models.PdlPersonalia>(
        summary = "Generelle personopplysninger",
        description = "Hentes fra PDL",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers pdl data"
        ),
        tags = setOf("PDL"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}

private object Models {
    @Serializable
    data class PdlPersonalia(
        val alder: Int,
        val bostedAdresse: List<Adresse>,
        val kontaktAdresse: List<Adresse>,
        val oppholdsAdresse: List<Adresse>,
    )

    @Serializable
    data class Adresse(
        val linje1: String,
        val linje2: String? = null,
        val linje3: String? = null,
    )
}
