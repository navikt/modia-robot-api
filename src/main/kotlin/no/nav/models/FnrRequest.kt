package no.nav.models

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable

@Serializable
data class FnrRequest(
    val fnr: String,
)

suspend fun ApplicationCall.deserializeFnr(): String? =
    try {
        this.receive<FnrRequest>().fnr
    } catch (e: ContentTransformationException) {
        null
    }
