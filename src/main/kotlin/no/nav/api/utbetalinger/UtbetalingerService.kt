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
            .flatMap { ytelse ->
                val ytelsekomponenter = ytelse.ytelseskomponentListe
                    ?.map { it.ytelseskomponenttype }
                    ?: listOf(requireNotNull(ytelse.ytelsestype))

                ytelsekomponenter.map { ytelsetype ->
                    Utbetalinger(
                        ytelse = ytelsetype,
                        fra = LocalDate.parse(ytelse.ytelsesperiode.fom),
                        til = LocalDate.parse(ytelse.ytelsesperiode.tom)
                    )
                }
            }
            .distinct()
            .sortedWith(compareByDescending<Utbetalinger> { it.til }.thenBy { it.ytelse })
    }
}
