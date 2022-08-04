package no.nav.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import no.nav.utils.UUIDSerializer

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = true
                serializersModule = SerializersModule {
                    contextual(UUIDSerializer)
                }
            }
        )
    }
}
