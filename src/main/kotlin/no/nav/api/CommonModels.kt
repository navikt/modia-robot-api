package no.nav.api

import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.ktor.http.*
import no.nav.plugins.HttpErrorResponse
import kotlin.reflect.typeOf

/**
 * Felles svar som routene deler.
 * Hver route kombinerer [standardResponses] med de svarene routen faktisk kan gi.
 */
object CommonModels {
    /**
     * Både `challenge`-blokken i sikkerhetsoppsettet og `StatusPages` svarer med samme
     * `HttpErrorResponse`, uavhengig av hvorfor tokenet ble avvist.
     */
    private val unauthorizedResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.Unauthorized)
            description(
                "Token mangler, er ugyldig eller utløpt, eller identen står ikke i tjenestens " +
                    "tillatelsesliste. Årsaken spesifiseres ikke i svaret.",
            )
            responseType(typeOf<HttpErrorResponse>())
        }

    private val internalServerErrorResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.InternalServerError)
            description("Uventet feil i tjenesten eller i en av de underliggende tjenestene")
            responseType(typeOf<HttpErrorResponse>())
        }


    val badRequestResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.BadRequest)
            description(
                "Forespørselen kunne ikke leses. For routes som tar en request-body: bodyen mangler, " +
                    "har feil Content-Type, eller er ikke gyldig JSON med de påkrevde feltene. For " +
                    "routes med parametere: en parameter mangler eller har ugyldig format. Feil i " +
                    "request-bodyen spesifiseres ikke i detalj, fordi meldingen fra deserialiseringen " +
                    "kan inneholde deler av bodyen.",
            )
            responseType(typeOf<HttpErrorResponse>())
        }

    /** Svar som kan komme fra alle routes i API-et. */
    val standardResponses =
        listOf(
            unauthorizedResponse,
            internalServerErrorResponse,
        )

    /** Brukes av routes som svarer uten innhold når det ikke finnes noe å returnere. */
    val noContentResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.NoContent)
            description("Forespørselen var gyldig, men det finnes ingen informasjon å returnere. Svaret har tom body.")
            responseType(typeOf<Unit>())
        }

    /** Brukes av oppslag på en identifikator som ikke finnes. */
    val notFoundResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.NotFound)
            description("Fant ingen ressurs med den oppgitte identifikatoren. Svaret har tom body.")
            responseType(typeOf<Unit>())
        }

    /** Kastes som `WebStatusException` når en underliggende tjeneste avviser tilgang. */
    val forbiddenResponse =
        ResponseInfo.builder {
            responseCode(HttpStatusCode.Forbidden)
            description("Den underliggende tjenesten avviste tilgang til opplysningene om brukeren")
            responseType(typeOf<HttpErrorResponse>())
        }
}
