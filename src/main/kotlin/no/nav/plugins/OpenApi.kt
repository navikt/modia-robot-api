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
import java.util.Properties
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
        swaggerUi()
    }
}

private const val SWAGGER_UI_PATH = "swagger-ui"
private const val SWAGGER_UI_ASSETS = "$SWAGGER_UI_PATH/assets"

/**
 * Server Swagger-UI fra webjar-en `org.webjars:swagger-ui` på klassestien.
 * Filene ble tidligere hentet fra unpkg.com, men er flyttet hit ettersom eksterne CDN-er er
 * blokkert i deler av Nav-nettet.
 */
private fun Route.swaggerUi() {
    val classLoader = application.environment.classLoader
    val versjon = swaggerUiVersjon(classLoader)

    // Filene leses ved oppstart, slik at en feilkonfigurert webjar oppdages med én gang
    // i stedet for som en 404 hos konsumenten
    fun ressurs(
        navn: String,
        contentType: ContentType,
    ) {
        val sti = "META-INF/resources/webjars/swagger-ui/$versjon/$navn"
        val innhold =
            checkNotNull(classLoader.lesRessurs(sti)) {
                "Fant ikke $sti i webjar-en org.webjars:swagger-ui"
            }
        get("$SWAGGER_UI_ASSETS/$navn") {
            call.response.header(HttpHeaders.CacheControl, "max-age=86400")
            call.respondBytes(innhold, contentType)
        }
    }

    ressurs("swagger-ui.css", ContentType.Text.CSS)
    ressurs("swagger-ui-bundle.js", ContentType.Application.JavaScript)

    get(SWAGGER_UI_PATH) {
        call.respondText(SWAGGER_UI_HTML, ContentType.Text.Html)
    }
}

/**
 * Versjonen leses fra webjar-ens egen `pom.properties` så den kun står i `build.gradle.kts`;
 * webjars legger filene under en mappe oppkalt etter versjonen.
 */
private fun swaggerUiVersjon(classLoader: ClassLoader): String {
    val pom = "META-INF/maven/org.webjars/swagger-ui/pom.properties"
    val egenskaper =
        checkNotNull(classLoader.lesRessurs(pom)) {
            "Fant ikke $pom. Mangler avhengigheten org.webjars:swagger-ui?"
        }.let { Properties().apply { load(it.inputStream()) } }
    return checkNotNull(egenskaper.getProperty("version")) { "Fant ingen `version` i $pom" }
}

private fun ClassLoader.lesRessurs(sti: String): ByteArray? = getResourceAsStream(sti)?.use { it.readBytes() }

private val SWAGGER_UI_HTML =
    """
    <!DOCTYPE html>
    <html lang="no">
    <head>
      <meta charset="utf-8"/>
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Modia Robot API</title>
      <link rel="stylesheet" type="text/css" href="/$SWAGGER_UI_ASSETS/swagger-ui.css">
    </head>
    <body>
    <div id="swagger-ui"></div>
    <script src="/$SWAGGER_UI_ASSETS/swagger-ui-bundle.js"></script>
    <script>
      SwaggerUIBundle({ url: '/openapi.json', dom_id: '#swagger-ui', presets: [SwaggerUIBundle.presets.apis] })
    </script>
    </body>
    </html>
    """.trimIndent()
