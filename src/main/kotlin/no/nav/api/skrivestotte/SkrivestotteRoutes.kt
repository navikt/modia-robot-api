package no.nav.api.skrivestotte

import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo
import io.ktor.http.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.plugins.securityScheme

fun Route.configureSkrivestotteRoutes() {
    route("skrivestotte/") {
        notarizedPost(Api.sok) {
            TODO()
        }
    }
}

private object Api {
    val sok = PostInfo<Unit, Models.Sok, Models.Response>(
        summary = "Tekster fra skrivestøtte",
        description = "Hentes fra modiapersonoversikt-skrivestotte",
        requestInfo = RequestInfo(
            description = "Søkestrengen som skal brukes mot skrivestøtte"
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Tekster som matcher søket"
        ),
        tags = setOf("Skrivestøtte"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}

private object Models {
    @Serializable
    data class Sok(
        val value: String
    )

    @Serializable
    data class Response(
        val tekster: List<Tekst>
    )

    @Serializable
    data class Tekst(
        val tittel: String,
        val innhold: String,
    )
}
