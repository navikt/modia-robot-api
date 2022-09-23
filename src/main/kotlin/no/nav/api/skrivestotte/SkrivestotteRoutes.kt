package no.nav.api.skrivestotte

import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme
import java.util.*

fun Route.configureSkrivestotteRoutes(
    skrivestotteService: SkrivestotteService,
) {
    route("skrivestotte/") {
        notarizedGet(Api.sok) {
            val sokeVerdi = call.request.queryParameters["sokeVerdi"]
            call.respond(skrivestotteService.hentTeksterFraSok(sokeVerdi))
        }
    }
    route("skrivestotte/{id}") {
        notarizedGet(Api.sokPaId) {
            val id = requireNotNull(call.parameters["id"])
            val tekst = skrivestotteService.hentTekstFraId(UUID.fromString(id))
            if (tekst != null) {
                call.respond(tekst)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private object Api {
    val sok = GetInfo<Models.SokeVerdiParameter, List<SkrivestotteClient.Tekst>>(
        summary = "Tekster fra skrivestøtte",
        description = "Hentes fra modiapersonoversikt-skrivestotte",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Tekster som matcher søket"
        ),
        tags = setOf("Skrivestøtte"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses
    )

    val sokPaId = GetInfo<Models.IdParameter, SkrivestotteClient.Tekst>(
        summary = "Tekst fra skrivestøtte gitt ID",
        description = "Hentes fra modiapersonoversikt-skrivestotte",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Tekst som matcher søket på ID"
        ),
        tags = setOf("Skrivestøtte"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses
    )
}

private object Models {

    open class IdParameter(
        @Param(type = ParamType.PATH)
        val id: UUID,
    )
    open class SokeVerdiParameter(
        @Param(type = ParamType.QUERY)
        val sokeVerdi: String?,
    )
}
