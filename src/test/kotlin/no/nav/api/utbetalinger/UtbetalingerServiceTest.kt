package no.nav.api.utbetalinger

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import no.nav.api.utbetalinger.UtbetalingerClient.*
import no.nav.plugins.WebStatusException
import no.nav.utils.BoundedOnBehalfOfTokenClient
import no.nav.utils.now
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UtbetalingerServiceTest {

    private val client: UtbetalingerClient = mockk<UtbetalingerClient>()
    private val service: UtbetalingerService = UtbetalingerService(client)

    @Test
    fun `skal hente ut og sortere utbetalinger for bruker`() {
        every { runBlocking { client.hentUtbetalinger(any(), any(), any(), any()) } } returns utbetalinger

        val utbetalinger = runBlocking {
            service.hentUtbetalinger("12345678910", LocalDate.now(), LocalDate.now(), "token")
        }

        assertEquals(8, utbetalinger.size)
        assertEquals("Alderspensjon som skal sorteres riktig alfabetisk", utbetalinger[2].ytelse)
        assertEquals("Dagpenger som også burde bli sortert på dato", utbetalinger[3].ytelse)
        assertEquals("Barnetillegg", utbetalinger[6].ytelse)
        assertEquals("Tiltakspenger", utbetalinger[7].ytelse)
        assertEquals(LocalDate(2022, 8, 1), utbetalinger[1].til)
    }

    @Test
    fun `skal feile i service når utbetalingV1 feiler`() {
        every { runBlocking { client.hentUtbetalinger(any(), any(), any(), any()) } } throws WebStatusException("Feil mot utbetalinger", HttpStatusCode.InternalServerError)
        assertThrows(WebStatusException::class.java) {
            runBlocking { service.hentUtbetalinger("12345678910", LocalDate.now(), LocalDate.now(), "token") }
        }
    }

    @Test
    fun `skal håndtere 404`() {
        val mockEngine = MockEngine { _ ->
            respond(
                status = HttpStatusCode.NotFound,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    "application/json"
                ),
                content = ""
            )
        }

        val oboToken = mockk<BoundedOnBehalfOfTokenClient>()
        every { oboToken.exchangeOnBehalfOfToken("token") } returns "new_token"

        val utbetalingerClient = UtbetalingerClient("http://no.no", oboToken, mockEngine)
        val utbetalinger = runBlocking {
            utbetalingerClient.hentUtbetalinger("10108000398", LocalDate.parse("2020-01-01"), LocalDate.now(), "token")
        }

        assertEquals(0, utbetalinger.size)
    }
}

fun medYtelse(type: String, fom: String, tom: String, komponentListe: List<YtelseKomponent>? = null) = Ytelse(
    ytelsestype = type,
    ytelsesperiode = Periode(
        fom = fom,
        tom = tom
    ),
    ytelseskomponentListe = komponentListe
)

val ytelser = listOf(
    medYtelse(
        type = "Dagpenger",
        fom = "2021-01-01",
        tom = "2021-05-01"
    ),
    medYtelse(
        type = "Dagpenger som går over ulike år",
        fom = "2021-12-01",
        tom = "2022-05-01"
    ),
    medYtelse(
        type = "Dagpenger som burde bli sortert på dato",
        fom = "2022-07-02",
        tom = "2022-08-01"
    ),
    medYtelse(
        type = "Dagpenger som også burde bli sortert på dato",
        fom = "2022-05-02",
        tom = "2022-07-01"
    ),
    medYtelse(
        type = "Alderspensjon som skal sorteres riktig alfabetisk",
        fom = "2022-05-02",
        tom = "2022-07-01"
    )
)

val utbetalinger = listOf(
    Utbetaling(
        utbetalingsstatus = "utbetalt",
        ytelseListe = ytelser
    ),
    Utbetaling(
        utbetalingsstatus = "ikke utbetalt",
        ytelseListe = ytelser
    ),
    Utbetaling(
        utbetalingsstatus = "utbetalt",
        ytelseListe = emptyList()
    ),
    Utbetaling(
        utbetalingsstatus = "utbetalt",
        ytelseListe = listOf(
            medYtelse(
                type = "Barnetrygd",
                fom = "2022-05-02",
                tom = "2025-07-01"
            )
        )
    ),
    Utbetaling(
        utbetalingsstatus = "utbetalt",
        ytelseListe = listOf(
            medYtelse(
                type = "Tiltakspenger",
                fom = "2020-08-01",
                tom = "2020-09-01",
                komponentListe = listOf(
                    YtelseKomponent("Tiltakspenger"),
                    YtelseKomponent("Barnetillegg")
                )
            )
        )
    )
)
