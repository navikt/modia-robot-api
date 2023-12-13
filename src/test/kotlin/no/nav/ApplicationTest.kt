package no.nav

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.plugins.configureMonitoring
import no.nav.plugins.configureOpenApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApplicationTest {
    @Test
    internal fun testRoot() {
        testApplication {
            application {
                configureOpenApi()
                configureMonitoring()
            }

            val response = client.get("/swagger-ui")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("swagger-ui"))
        }
    }
}
