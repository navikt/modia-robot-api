package no.nav.api.utbetalinger

import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import no.nav.api.utbetalinger.UtbetalingerClient.*
import no.nav.plugins.WebStatusException
import no.nav.utils.now
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

fun medYtelse(type: String, fom: String, tom: String) = Ytelse(
    ytelsestype = type,
    ytelsesperiode = Periode(
        fom = fom,
        tom = tom
    )
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
    ),
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
)