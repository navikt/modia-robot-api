package no.nav.plugins

import io.bkbn.kompendium.core.attribute.KompendiumAttributes
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.component.Components
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.security.BearerAuth
import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

/**
 * By default we want `null` present in our responses, but not when returning the OpenAPI-spec
 * Hence a custom serializer is used here
 */
@OptIn(ExperimentalSerializationApi::class)
private val specSerializer =
    Json {
        serializersModule = KompendiumSerializersModule.module
        encodeDefaults = true
        explicitNulls = false
    }

fun Application.configureOpenApi() {
    install(NotarizedApplication()) {
        spec = {
            OpenApiSpec(
                jsonSchemaDialect = "https://spec.openapis.org/oas/3.1/dialect/base",
                info =
                    Info(
                        "modia-robot-api",
                        version = "1.0.0",
                    ),
                components =
                    Components(
                        securitySchemes =
                            mutableMapOf(
                                SECURITY_SCHEME_NAME to BearerAuth(),
                            ),
                    ),
            )
        }

        specRoute = { spec, routing ->
            routing.route("/openapi.json") {
                get {
                    call.respondText(
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK,
                        text = specSerializer.encodeToString(this@route.application.attributes[KompendiumAttributes.openApiSpec]),
                    )
                }
            }
        }

        customTypes =
            mapOf(
                typeOf<kotlinx.datetime.Instant>() to TypeDefinition(type = "string", format = "date-time"),
                typeOf<kotlinx.datetime.LocalDate>() to TypeDefinition(type = "string", format = "date"),
                typeOf<kotlinx.datetime.LocalDateTime>() to TypeDefinition(type = "string", format = "date-time"),
            )
    }

    routing {
        get("swagger-ui") {
            call.respondText(
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.OK,
                text = """<!DOCTYPE html>
                        <html>
                        <head>
                          <title>Modia Robot API</title>
                          <meta charset="utf-8"/>
                          <meta name="viewport" content="width=device-width, initial-scale=1">
                          <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist/swagger-ui.css">
                        </head>
                        <body>
                        <div id="swagger-ui"></div>
                        <script src="https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js"></script>
                        <script>
                          SwaggerUIBundle({ url: '/openapi.json', dom_id: '#swagger-ui', presets: [SwaggerUIBundle.presets.apis] })
                        </script>
                        </body>
                        </html>""",
            )
        }
    }
}
