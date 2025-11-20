package no.nav.api.pdl

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import no.nav.utils.BoundedOnBehalfOfTokenClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PdlTest {
    @Test
    fun `should be able to deserialize pdl response`() =
        runBlocking {
            val mockEngine =
                MockEngine { request ->
                    val body = (request.body as TextContent).text
                    assertTrue(body.contains("variables"))
                    assertTrue(body.contains("hentPerson"))
                    respond(
                        status = HttpStatusCode.OK,
                        headers =
                            headersOf(
                                HttpHeaders.ContentType,
                                "application/json",
                            ),
                        content =
                            """
                            {
                                "data": {
                                    "hentPerson": {
                                    "navn": [
                                        { "fornavn": "Ola", "mellomnavn": null, "etternavn": "Nordmann" }
                                    ],
                                        "foedselsdato": [
                                            { "foedselsdato": "2020-06-06" }
                                        ],
                                         "doedsfall": [],
                                        "oppholdsadresse": [],
                                        "kontaktadresse": [],
                                        "bostedsadresse": []
                                    }
                                }
                            }
                            """.trimIndent(),
                    )
                }
            val tokenClient = mockk<BoundedOnBehalfOfTokenClient>()
            every { tokenClient.exchangeOnBehalfOfToken("token") } returns "new_token"

            val pdlClient = PdlClient("http://no.no", tokenClient, mockEngine)
            val person = pdlClient.hentPersonalia("10108000398", "token")

            assertEquals(
                1,
                person.data
                    ?.hentPerson
                    ?.foedselsdato
                    ?.size,
            )
            assertEquals(
                LocalDate(2020, 6, 6),
                person.data
                    ?.hentPerson
                    ?.foedselsdato
                    ?.get(0)
                    ?.foedselsdato,
            )

            assertEquals(
                "Nordmann",
                person.data
                    ?.hentPerson
                    ?.navn
                    ?.first()
                    ?.etternavn,
            )
        }

    @Test
    fun `should be able to get aktorID for ident`() =
        runBlocking {
            val mockEngine =
                MockEngine { request ->
                    val body = (request.body as TextContent).text
                    assertTrue(body.contains("variables"))
                    assertTrue(body.contains("hentIdenter"))
                    respond(
                        status = HttpStatusCode.OK,
                        headers =
                            headersOf(
                                HttpHeaders.ContentType,
                                "application/json",
                            ),
                        content =
                            """
                            {
                                "data": {
                                    "hentIdenter": {
                                        "identer": [
                                            { "ident": "123456789" }
                                        ]
                                    }
                                }
                            }
                            """.trimIndent(),
                    )
                }
            val tokenClient = mockk<BoundedOnBehalfOfTokenClient>()
            every { tokenClient.exchangeOnBehalfOfToken("token") } returns "new_token"

            val pdlClient = PdlClient("http://no.no", tokenClient, mockEngine)
            val identer = pdlClient.hentAktorid("10108000398", "token")

            assertEquals(
                1,
                identer.data
                    ?.hentIdenter
                    ?.identer
                    ?.size,
            )
            assertEquals(
                "123456789",
                identer.data
                    ?.hentIdenter
                    ?.identer
                    ?.get(0)
                    ?.ident,
            )
        }
}
