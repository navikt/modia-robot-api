package no.nav.api.kodeverk

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.utils.BoundedMachineToMachineTokenClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class KodeverkTest {
    @Test
    fun `should be able to deserialize kodeverk response`() =
        runBlocking {
            val mockEngine =
                MockEngine { _ ->
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
                              "betydninger": {
                                "0660": [
                                  {
                                    "gyldigFra": "2025-09-11",
                                    "gyldigTil": "2025-09-11",
                                    "beskrivelser": {
                                      "nb": {
                                        "term": "Oslo",
                                        "tekst": "lengre tekst"
                                      }
                              
                                    }
                                  }
                                ],
                                "5003": [
                                  {
                                    "gyldigFra": "2025-09-11",
                                    "gyldigTil": "2025-09-11",
                                    "beskrivelser": {
                                      "nb": {
                                        "term": "Bergen",
                                        "tekst": "lengre tekst"
                                      }
                                    
                                    }
                                  }
                                ],
                                "6893": [
                                  {
                                    "gyldigFra": "2025-09-11",
                                    "gyldigTil": "2025-09-11",
                                    "beskrivelser": {
                                      "nb": {
                                        "term": "Vik i Sogn",
                                        "tekst": "lengre tekst"
                                      }
                                    
                                    }
                                  }
                                ]
                              }
                            }
                            """.trimIndent(),
                    )
                }
            val tokenClient = mockk<BoundedMachineToMachineTokenClient>()
            every { tokenClient.createMachineToMachineToken() } returns "new_token"

            val kodeverkClient = KodeverkClient("http://no.no", tokenClient, mockEngine)
            val kodeverkPostnummer = kodeverkClient.hentKodeverkRaw("Postnummer")

            val kodeverkService = KodeverkService(kodeverkClient)
            val kodeverkBetydninger = kodeverkService.parseTilKodeverk(kodeverkPostnummer)

            assertEquals(
                3,
                kodeverkPostnummer.betydninger.size,
            )

            assertTrue(kodeverkBetydninger.containsKey("0660"))
            assertTrue(kodeverkBetydninger.containsKey("6893"))
            assertEquals("Oslo", kodeverkBetydninger["0660"])
            assertEquals("Bergen", kodeverkBetydninger["5003"])
        }
}
