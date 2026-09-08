package no.nav

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.plugins.configureMonitoring
import no.nav.plugins.configureOpenApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Sikrer at Swagger-UI faktisk er brukbar, ikke bare at ruten svarer.
 *
 * Bakgrunn: siden lastet tidligere `swagger-ui-bundle.js` og `swagger-ui.css` fra `unpkg.com`.
 * For brukere i nettverkssoner der eksterne CDN-er er blokkert feilet de kallene med
 * `ERR_CONNECTION_CLOSED`, og siden ble blank med «SwaggerUIBundle is not defined».
 * Den gamle testen fanget det ikke, fordi selve HTML-en ble levert helt fint.
 */
class SwaggerUiTest {
    @Test
    fun `swagger-ui laster ingen ressurser fra eksterne domener`() =
        medSwaggerUi {
            val eksterneReferanser =
                EKSTERN_REFERANSE
                    .findAll(hentSwaggerUiHtml())
                    .map { it.groupValues[1] }
                    .toList()

            assertTrue(
                eksterneReferanser.isEmpty(),
                "Swagger-UI refererer til eksterne ressurser: $eksterneReferanser. " +
                    "Eksterne CDN-er er blokkert i deler av Nav-nettet, og siden blir da blank for " +
                    "konsumentene. Ressursene skal serveres fra applikasjonen selv.",
            )
        }

    @Test
    fun `alle ressursene swagger-ui refererer til kan hentes fra applikasjonen`() =
        medSwaggerUi {
            val referanser =
                LOKAL_REFERANSE
                    .findAll(hentSwaggerUiHtml())
                    .map { it.groupValues[1] }
                    .distinct()
                    .toList()

            assertTrue(
                referanser.containsAll(PAAKREVDE_RESSURSER),
                "Swagger-UI-siden refererer til $referanser, men mangler $PAAKREVDE_RESSURSER",
            )

            referanser.forEach { path ->
                val response = client.get(path)

                assertEquals(
                    HttpStatusCode.OK,
                    response.status,
                    "Swagger-UI refererer til $path, men ressursen finnes ikke. " +
                        "Ligger filene på forventet sti i webjar-en org.webjars:swagger-ui?",
                )
                assertTrue(
                    response.bodyAsText().isNotEmpty(),
                    "$path ble levert tom",
                )
            }
        }

    @Test
    fun `swagger-ui starter opp mot vår egen spesifikasjon`() =
        medSwaggerUi {
            val html = hentSwaggerUiHtml()

            assertTrue(html.contains("SwaggerUIBundle("), "Mangler oppstartskallet til Swagger-UI")
            assertTrue(html.contains("url: '/openapi.json'"), "Swagger-UI peker ikke på /openapi.json")
        }

    private suspend fun ApplicationTestBuilder.hentSwaggerUiHtml(): String {
        val response = client.get("/$SWAGGER_UI_STI")

        assertEquals(HttpStatusCode.OK, response.status, "Fikk ikke levert Swagger-UI-siden")
        return response.bodyAsText()
    }

    private fun medSwaggerUi(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            application {
                configureOpenApi()
                configureMonitoring()
            }
            block()
        }
    }

    private companion object {
        const val SWAGGER_UI_STI = "swagger-ui"

        val PAAKREVDE_RESSURSER =
            listOf(
                "/$SWAGGER_UI_STI/assets/swagger-ui.css",
                "/$SWAGGER_UI_STI/assets/swagger-ui-bundle.js",
            )

        /** `src`/`href` som peker på et annet domene enn applikasjonen selv. */
        val EKSTERN_REFERANSE = Regex("""(?:src|href)\s*=\s*["']((?:https?:)?//[^"']+)["']""")

        /** `src`/`href` som peker på en absolutt sti i applikasjonen. */
        val LOKAL_REFERANSE = Regex("""(?:src|href)\s*=\s*["'](/[^"']+)["']""")
    }
}
