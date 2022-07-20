package no.nav.api.digdir

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme

fun Route.configureDigdirRoutes() {
    route("digdir/{fnr}/epost") {
        notarizedGet(Api.epost) {
            call.respond(Models.Epost(
                value = "testing",
                sistOppdatert = null,
                sistVerifisert = Clock.System.todayIn(TimeZone.currentSystemDefault())
            ))
        }
    }
}
private object Api {
    val epost = GetInfo<CommonModels.FnrParameter, Models.Epost>(
        summary = "Brukers epost",
        description = "Hentes fra digdir-proxy",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers epost"
        ),
        tags = setOf("Kontakt og reservasjons registeret"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}

private object Models {
    @Serializable
    data class Epost(
        val value: String,
        val sistOppdatert: LocalDate? = null,
        val sistVerifisert: LocalDate? = null,
    )
}
