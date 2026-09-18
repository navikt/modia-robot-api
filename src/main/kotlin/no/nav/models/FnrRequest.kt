package no.nav.models

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable

@Serializable
data class FnrRequest(
    val fnr: String,
)

/**
 * Leser fødselsnummeret fra request-body.
 *
 * Kaster ved manglende eller ugyldig body. `StatusPages` oversetter det til 400, slik at alle routes
 * svarer likt. Feilen håndteres sentralt fordi meldingen fra deserialiseringen kan inneholde selve
 * request-bodyen, og den skal ikke ut til konsumenten eller i applikasjonsloggen.
 */
suspend fun ApplicationCall.deserializeFnr(): String = receive<FnrRequest>().fnr
