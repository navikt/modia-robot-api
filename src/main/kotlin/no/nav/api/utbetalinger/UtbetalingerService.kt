package no.nav.api.utbetalinger

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.personoversikt.utils.SelftestGenerator
import no.nav.utils.toKotlinLocalDate
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes

class UtbetalingerService(
    private val utbetalingerClient: UtbetalingerClient,
    private val utbetalingerRestClient: UtbetalingerRestClient
) {
    
    private val reporter = SelftestGenerator.Reporter(name = "UtbetalingService", critical = false)
        .also { it.reportOk() }
    
    init {
        fixedRateTimer(name = "Ping utbetaldata", daemon = true, period = 1.minutes.inWholeMilliseconds, initialDelay = 0) {
            runBlocking {
                reporter.ping {
                    utbetalingerClient.ping()
                }
            }
        }
    }
    
    @Serializable
    data class Utbetalinger(
        val ytelse: String,
        val fra: LocalDate,
        val til: LocalDate
    )
    
    suspend fun hentUtbetalinger(fnr: String, fra: LocalDate, til: LocalDate): List<Utbetalinger> {
        val utbetalinger = utbetalingerClient.hentUtbetalinger(fnr, fra, til)
        val utbetalteYtelser = utbetalinger
            .filter { it.utbetalingsstatus.lowercase() == "utbetalt" }
            .flatMap { it.ytelseListe }
        return utbetalteYtelser
            .map {
                Utbetalinger(
                    ytelse = it.ytelsestype.value,
                    fra = it.ytelsesperiode.fom.toKotlinLocalDate(),
                    til = it.ytelsesperiode.tom.toKotlinLocalDate()
                )
            }
            .sortedWith(compareByDescending<Utbetalinger> { it.til }.thenBy { it.ytelse })
    }
    
    suspend fun hentRestUtbetalinger(fnr: String, fra: LocalDate, til: LocalDate): List<Utbetalinger> {
        val utbetalinger = utbetalingerRestClient.hentUtbetalinger(fnr, fra, til)
            .also { reporter.reportOk() }
        val utbetalteYtelser = utbetalinger
            .filter { it.utbetalingsstatus.lowercase() == "utbetalt" }
            .flatMap { it.ytelseListe }
        return utbetalteYtelser
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