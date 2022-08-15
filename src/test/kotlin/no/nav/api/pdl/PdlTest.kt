package no.nav.api.pdl

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import no.nav.utils.BoundedMachineToMachineTokenClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


internal class PdlTest {
    @Test
    fun `should be able to deserialize pdl response`() = runBlocking {
        val mockEngine = MockEngine { request ->
            val body = (request.body as TextContent).text
            assertTrue(body.contains("variables"))
            assertTrue(body.contains("hentPerson"))
            respond(
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType, "application/json"
                ),
                content = """
                    {
                        "data": {
                            "hentPerson": {
                                "foedsel": [
                                    { "foedselsdato": "2020-06-06" }
                                ],
                                "oppholdsadresse": [],
                                "kontaktadresse": [],
                                "bostedsadresse": []
                            }
                        }
                    }
                """.trimIndent(),
            )
        }
        val tokenClient = mockk<BoundedMachineToMachineTokenClient>()
        every { tokenClient.createMachineToMachineToken() } returns ""

        val pdlClient = PdlClient("http://no.no", tokenClient, mockEngine)
        val person = pdlClient.hentPersonalia("10108000398")

        assertEquals(1, person.data?.hentPerson?.foedsel?.size)
        assertEquals(LocalDate(2020, 6, 6), person.data?.hentPerson?.foedsel?.get(0)?.foedselsdato)
    }
}