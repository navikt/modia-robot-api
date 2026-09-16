package no.nav.api

import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import no.nav.plugins.HttpErrorResponse
import kotlin.reflect.typeOf

object CommonModels {
    val fnrParameter =
        Parameter(
            name = "fnr",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.STRING,
        )

    private val noContentResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.NoContent)
            description("Ingen informasjon funnet")
            responseType(typeOf<Unit>())
        }

    private val internalServerErrorResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.InternalServerError)
            description("Ingen informasjon funnet")
            responseType(typeOf<Unit>())
        }

    val standardResponses =
        listOf(
            noContentResponse,
            internalServerErrorResponse,
        )

    val forbiddenResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.Forbidden)
            description("Den underliggende tjenesten avviste tilgang til opplysningene om brukeren")
            responseType(typeOf<HttpErrorResponse>())
        }

    val notFoundResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.NotFound)
            description("Fant ingen person med den oppgitte identen")
            responseType(typeOf<HttpErrorResponse>())
        }
}
