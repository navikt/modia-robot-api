package no.nav.plugins

import io.bkbn.kompendium.core.Kompendium
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.schema.SimpleSchema
import io.bkbn.kompendium.oas.schema.TypedSchema
import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.bkbn.kompendium.swagger.JsConfig
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.plugins.swaggerui.SwaggerUI2
import java.net.URI
import kotlin.reflect.KClass

/**
 * By default we want `null` present in our responses, but not when returning the OpenAPI-spec
 * Hence a custom serializer is used here
 */
@OptIn(ExperimentalSerializationApi::class)
private val specSerializer = Json {
    serializersModule = KompendiumSerializersModule.module
    encodeDefaults = true
    explicitNulls = false
}

fun Application.configureOpenApi() {
    install(Kompendium) {
        spec = OpenApiSpec(
            info = Info(
                "modia-robot-api",
                version = "1.0.0"
            )
        )

        openApiJson = { spec ->
            route("/openapi.json") {
                get {
                    call.respondText(
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK,
                        text = specSerializer.encodeToString(spec)
                    )
                }
            }
        }

        addCustomSchema(kotlinx.datetime.Instant::class, SimpleSchema("string", format = "date-time"))
        addCustomSchema(kotlinx.datetime.LocalDate::class, SimpleSchema("string", format = "date"))
        addCustomSchema(kotlinx.datetime.LocalDateTime::class, SimpleSchema("string", format = "date-time"))
    }

    install(SwaggerUI2) {
        jsConfig = JsConfig(
            specs = mapOf(
                "Version 1" to URI("/openapi.json")
            ),
            validatorUrl = "none",
        )
    }
}

private fun Kompendium.Configuration.addCustomSchema(clazz: KClass<*>, schema: TypedSchema) {
    bodyCache[clazz.simpleName!!] = schema
    parameterCache[clazz.simpleName!!] = schema
}