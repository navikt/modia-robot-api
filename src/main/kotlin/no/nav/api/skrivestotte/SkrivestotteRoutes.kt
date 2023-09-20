package no.nav.api.skrivestotte

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import java.util.*
import kotlin.reflect.typeOf

fun Route.configureSkrivestotteRoutes(
    skrivestotteService: SkrivestotteService,
) {
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
    val sok = GetInfo.builder {
        summary("Tekster fra skrivestøtte")
        description("Hentes fra modiapersonoversikt-skrivestotte")
        request {
            parameters(Models.sokeVerdiParameter)
        }
        response {
            responseType(typeOf<SkrivestotteClient.Tekst>())
            responseCode(HttpStatusCode.OK)
            description("Tekst som matcher søket")
        }
        tags("Skrivestøtte")
        canRespond(CommonModels.standardResponses)
    }
    val sokPaId = GetInfo.builder {
        summary("Tekst fra skrivestøtte gitt ID")
        description("Hentes fra modiapersonoversikt-skrivestotte")
        request { parameters(Models.idParameter) }
        tags("Skrivestøtte")

        response {
            responseType(typeOf<SkrivestotteClient.Tekst>())
            responseCode(HttpStatusCode.OK)
            description("Tekst som matcher søket på ID")
        }
        tags("Skrivestøtte")
        canRespond(CommonModels.standardResponses)
    }
}

private object Models {
    val idParameter = Parameter(
        name = "id",
        `in` = Parameter.Location.path,
        schema = TypeDefinition.STRING,
    )

    val sokeVerdiParameter = Parameter(
        name = "sokeVerdi",
        `in` = Parameter.Location.query,
        schema = TypeDefinition.STRING,
    )
}
