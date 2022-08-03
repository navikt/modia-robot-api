package no.nav

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
        withTestApplication({ configureOpenApi(); configureMonitoring() }) {
            handleRequest(HttpMethod.Get, "/webjars/swagger-ui/index.html").apply {
                assertEquals(response.status(), HttpStatusCode.OK)
                assertTrue(response.content?.contains("swagger-ui") ?: false)
            }
        }
    }
}
