package no.nav.api.utbetalinger

import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import no.nav.plugins.WebStatusException
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSPeriode
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSUtbetaling
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSYtelse
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSYtelsestyper
import no.nav.utils.now
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UtbetalingerServiceTest {
    
    private val client: UtbetalingerClient = mockk()
    private val service: UtbetalingerService = UtbetalingerService(client)
    
    @Test
    fun `skal hente ut og sortere utbetalinger for bruker`() {
        every { runBlocking { client.hentUtbetalinger(any(), any(), any()) } } returns utbetalinger
        
        val utbetalinger = runBlocking {
            service.hentUtbetalinger("12345678910", LocalDate.now(), LocalDate.now())
        }
        
        assertEquals(6, utbetalinger.size)
        assertEquals("Alderspensjon som skal sorteres riktig alfabetisk", utbetalinger[2].ytelse)
        assertEquals("Dagpenger som også burde bli sortert på dato", utbetalinger[3].ytelse)
        assertEquals(LocalDate(2022, 8, 1), utbetalinger[1].til)
    }
    
    @Test
    fun `skal feile i service når utbetalingV1 feiler`() {
        every { runBlocking { client.hentUtbetalinger(any(), any(), any()) } } throws WebStatusException("Feil mot utbetalinger", HttpStatusCode.InternalServerError)
        assertThrows(WebStatusException::class.java) {
            runBlocking { service.hentUtbetalinger("12345678910", LocalDate.now(), LocalDate.now()) }
        }
    }
    
}

fun medYtelse(type: String, fra: DateTime, til: DateTime): WSYtelse =
    WSYtelse()
    .withYtelsestype(WSYtelsestyper().withValue(type))
    .withYtelsesperiode(
        WSPeriode()
            .withFom(fra)
            .withTom(til)
    )

val ytelser = listOf(
    medYtelse(
        type = "Dagpenger",
        fra = DateTime(2021, 1, 1, 0, 0),
        til = DateTime(2021, 5, 1, 0, 0)
    ),
    medYtelse(
        type = "Dagpenger som går over ulike år",
        fra = DateTime(2021, 12, 1, 0, 0),
        til = DateTime(2022, 5, 1, 0, 0)
    ),
    medYtelse(
        type = "Dagpenger som burde bli sortert på dato",
        fra = DateTime(2022, 7, 2, 0, 0),
        til = DateTime(2022, 8, 1, 0, 0)
    ),
    medYtelse(
        type = "Dagpenger som også burde bli sortert på dato",
        fra = DateTime(2022, 5, 2, 0, 0),
        til = DateTime(2022, 7, 1, 0, 0)
    ),
    medYtelse(
        type = "Alderspensjon som skal sorteres riktig alfabetisk",
        fra = DateTime(2022, 5, 2, 0, 0),
        til = DateTime(2022, 7, 1, 0, 0)
    )
)

val utbetalinger = listOf<WSUtbetaling>(
    WSUtbetaling()
        .withUtbetalingsstatus("utbetalt")
        .withYtelseListe(ytelser),
    WSUtbetaling()
        .withUtbetalingsstatus("ikke utbetalt")
        .withYtelseListe(ytelser),
    WSUtbetaling()
        .withUtbetalingsstatus("utbetalt")
        .withUtbetalingsmelding("Denne har ingen ytelser knyttet til seg"),
    WSUtbetaling()
        .withUtbetalingsstatus("utbetalt")
        .withYtelseListe(
            listOf(
                medYtelse(
                    type = "Barnetrygd",
                    fra = DateTime(2022, 5, 2, 0, 0),
                    til = DateTime(2025, 7, 1, 0, 0)
                )
            )
        ),
    
)