package no.nav.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*

class WebStatusException(message: String, val status: HttpStatusCode) : Exception(message)

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<WebStatusException> { cause ->
            call.respond(
                cause.status,
                HttpErrorResponse(
                    cause = cause.toString(),
                    message = cause.message,
                    
                )
            )
        }
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

internal data class HttpErrorResponse(
    val message: String? = null,
    val cause: String? = null
)
