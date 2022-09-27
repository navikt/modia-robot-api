package no.nav.api.utbetalinger

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.personoversikt.utils.SelftestGenerator

class UtbetalingerService(
    private val utbetalingerClient: UtbetalingerClient,
) {

    private val reporter = SelftestGenerator.Reporter(name = "UtbetalingService", critical = false)
        .also { it.reportOk() }

    @Serializable
    data class Utbetalinger(
        val ytelse: String,
        val fra: LocalDate,
        val til: LocalDate,
    )

    suspend fun hentUtbetalinger(fnr: String, fra: LocalDate, til: LocalDate): List<Utbetalinger> {
        val utbetalinger = utbetalingerClient.runCatching {
            hentUtbetalinger(fnr, fra, til)
        }
            .onSuccess { reporter.reportOk() }
            .onFailure { reporter.reportError(it) }
            .getOrThrow()

        return utbetalinger
            .filter { it.utbetalingsstatus.lowercase() == "utbetalt" }
            .flatMap { it.ytelseListe }
            .map {
                val ytelse = requireNotNull(it.ytelsestype)
                Utbetalinger(
                    ytelse = ytelse,
                    fra = LocalDate.parse(it.ytelsesperiode.fom),
                    til = LocalDate.parse(it.ytelsesperiode.tom)
                )
            }
            .sortedWith(compareByDescending<Utbetalinger> { it.til }.thenBy { it.ytelse })
    }
}
