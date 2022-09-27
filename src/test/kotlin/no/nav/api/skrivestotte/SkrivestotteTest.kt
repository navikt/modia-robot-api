package no.nav.api.skrivestotte

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SkrivestotteTest {
    @Test
    internal fun name() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    "application/json"
                ),
                content = """
                            {
                                "5859002e-920b-43d7-a730-8ed346385186": {
                                    "id": "5859002e-920b-43d7-a730-8ed346385186", 
                                    "overskrift": "Dette er en overskrift", 
                                    "tags": ["ks"], 
                                    "innhold": {
                                        "nb_NO": "Innhold her"                            
                                    } 
                                }
                            }
                """.trimIndent()
            )
        }

        val skrivestotteClient = SkrivestotteClient("http://no.no", mockEngine)
        val tekster = skrivestotteClient.hentTekster()

        assertEquals(1, tekster.size)
    }
}
