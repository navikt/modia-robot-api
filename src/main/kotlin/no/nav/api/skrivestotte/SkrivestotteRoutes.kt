package no.nav.api.skrivestotte

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.plugins.HttpErrorResponse
import java.util.*
import kotlin.reflect.typeOf

fun Route.configureSkrivestotteRoutes(skrivestotteService: SkrivestotteService) {
    route("skrivestotte/") {
        install(NotarizedRoute()) {
            get = Api.sok
        }
        get {
            val sokeVerdi = call.request.queryParameters["sokeVerdi"]
            call.respond(skrivestotteService.hentTeksterFraSok(sokeVerdi))
        }
    }
    route("skrivestotte/{id}") {
        install(NotarizedRoute()) {
            get = Api.sokPaId
        }
        get {
            val id = requireNotNull(call.parameters["id"])
            val tekstId =
                runCatching { UUID.fromString(id) }.getOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        HttpErrorResponse(message = "«$id» er ikke en gyldig UUID"),
                    )
            val tekst = skrivestotteService.hentTekstFraId(tekstId)
            if (tekst != null) {
                call.respond(tekst)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private object Api {
    val sok =
        GetInfo.builder {
            summary("Tekster fra skrivestøtte")
            description("Hentes fra modiapersonoversikt-skrivestotte")
            request {
                parameters(Models.sokeVerdiParameter)
            }
            response {
                responseType(typeOf<List<SkrivestotteClient.Tekst>>())
                responseCode(HttpStatusCode.OK)
                description("Tekster som matcher søket. Tom liste når ingenting matcher.")
            }
            tags("Skrivestøtte")
            canRespond(CommonModels.standardResponses)
        }
    val sokPaId =
        GetInfo.builder {
            summary("Tekst fra skrivestøtte gitt ID")
            description("Hentes fra modiapersonoversikt-skrivestotte")
            request { parameters(Models.idParameter) }

            response {
                responseType(typeOf<SkrivestotteClient.Tekst>())
                responseCode(HttpStatusCode.OK)
                description("Tekst som matcher søket på ID")
            }
            tags("Skrivestøtte")
            canRespond(
                CommonModels.standardResponses + CommonModels.badRequestResponse + CommonModels.notFoundResponse,
            )
        }
}

private object Models {
    val idParameter =
        Parameter(
            name = "id",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.UUID,
            description = "Identifikatoren til teksten, som UUID",
        )

    val sokeVerdiParameter =
        Parameter(
            name = "sokeVerdi",
            `in` = Parameter.Location.query,
            schema = TypeDefinition.STRING,
            required = false,
            description =
                "Ord som må forekomme i overskrift, tagger eller innhold. Flere ord skilles med " +
                    "mellomrom, og alle må matche. Utelates parameteren, returneres alle tekster.",
        )
}
