package no.nav.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import no.nav.utils.TjenestekallLogger

class WebStatusException(
    message: String,
    val status: HttpStatusCode,
) : Exception(message)

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<WebStatusException> { call, cause ->
            this@configureExceptionHandling.log.warn("WebStatusException:${cause.status.value}", cause)
            call.respond(
                cause.status,
                HttpErrorResponse(
                    cause = cause.toString(),
                    message = cause.message,
                ),
            )
        }
        exception<BadRequestException> { call, cause ->
            call.besvarUgyldigForesporsel(this@configureExceptionHandling, cause)
        }
        exception<ContentTransformationException> { call, cause ->
            call.besvarUgyldigForesporsel(this@configureExceptionHandling, cause)
        }
        exception<Throwable> { call, cause ->
            this@configureExceptionHandling.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpErrorResponse(
                    cause = cause.toString(),
                    message = cause.message,
                ),
            )
        }
        status(HttpStatusCode.Unauthorized) { statusCode ->
            val arsaker = call.authentication.allFailures.joinToString("\n") { it.prettyPrint() }
            this@configureExceptionHandling.log.warn("Avviste forespørsel med 401: $arsaker")
            call.respond(statusCode, UGYLDIG_TOKEN)
        }
    }
}

/**
 * Samme svar uansett hva som var galt med forespørselen.
 *
 * Detaljene logges i teamloggen, ikke i applikasjonsloggen og ikke i svaret: kotlinx.serialization
 * tar med deler av request-bodyen i feilmeldingen sin, og den kan inneholde fødselsnummer.
 */
private suspend fun ApplicationCall.besvarUgyldigForesporsel(
    application: Application,
    cause: Throwable,
) {
    application.log.warn("Avviste forespørsel med 400: ${cause.message}")
    TjenestekallLogger.warn(
        "Ugyldig forespørsel",
        mapOf("exception" to cause.stackTraceToString()),
    )
    respond(HttpStatusCode.BadRequest, UGYLDIG_FORESPORSEL)
}

internal val UGYLDIG_FORESPORSEL =
    HttpErrorResponse(
        message =
            "Forespørselen kunne ikke leses. Kontroller at Content-Type er application/json og at " +
                "bodyen er gyldig JSON med de påkrevde feltene.",
    )

/**
 * Samme svar uansett hvorfor tokenet ble avvist.
 *
 * Årsaken logges, men eksponeres ikke: `AuthenticationFailedCause.Error` kan inneholde detaljer fra
 * JWT-valideringen, og forskjellen på «mangler token» og «ikke i tillatelseslisten» er informasjon
 * en uautorisert klient ikke trenger.
 */
internal val UGYLDIG_TOKEN =
    HttpErrorResponse(
        message = "Token mangler, er ugyldig eller utløpt, eller identen har ikke tilgang til denne tjenesten",
    )

private fun AuthenticationFailedCause.prettyPrint(): String =
    when (this) {
        AuthenticationFailedCause.NoCredentials -> "No credentials"
        AuthenticationFailedCause.InvalidCredentials -> "Invalid credentials"
        is AuthenticationFailedCause.Error -> "Error with credentials: ${this.message}"
    }

@Serializable
data class HttpErrorResponse(
    val message: String? = null,
    val cause: String? = null,
)
