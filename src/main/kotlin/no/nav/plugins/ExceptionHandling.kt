package no.nav.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.Serializable

class WebStatusException(message: String, val status: HttpStatusCode) : Exception(message)

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<WebStatusException> { cause ->
            log.warn("WebStatusException:${cause.status.value}", cause)
            call.respond(
                cause.status,
                HttpErrorResponse(
                    cause = cause.toString(),
                    message = cause.message
                )
            )
        }
        exception<Throwable> { cause ->
            log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpErrorResponse(
                    cause = cause.toString(),
                    message = cause.message
                )
            )
        }
        status(HttpStatusCode.Unauthorized) { statusCode ->
            val message = call.authentication.allFailures.joinToString("\n") { it.prettyPrint() }
            log.error(message)
            call.respond(statusCode, message)
        }
    }
}

private fun AuthenticationFailedCause.prettyPrint(): String {
    return when (this) {
        AuthenticationFailedCause.NoCredentials -> "No credentials"
        AuthenticationFailedCause.InvalidCredentials -> "Invalid credentials"
        is AuthenticationFailedCause.Error -> "Error with credentials: ${this.message}"
    }
}

@Serializable
internal data class HttpErrorResponse(
    val message: String? = null,
    val cause: String? = null,
)
