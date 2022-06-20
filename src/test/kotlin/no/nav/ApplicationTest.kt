package no.nav

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.plugins.configureMonitoring
import no.nav.plugins.configureOpenApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ configureOpenApi(); configureMonitoring() }) {
            handleRequest(HttpMethod.Get, "/webjars/swagger-ui/index.html").apply {
                assertEquals(response.status(), HttpStatusCode.OK)
                assertContains(response.content ?: "", "swagger-ui")
            }
        }
    }
}
