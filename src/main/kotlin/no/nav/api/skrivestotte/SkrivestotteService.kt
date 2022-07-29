package no.nav.api.skrivestotte

import kotlinx.coroutines.runBlocking
import no.nav.api.skrivestotte.SkrivestotteClient.*
import no.nav.personoversikt.utils.Retry
import no.nav.personoversikt.utils.SelftestGenerator
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class SkrivestotteService (private val skrivestotteClient: SkrivestotteClient) {
    
    private var teksterCache: Map<UUID, Tekst> = emptyMap()
    private var sokbareTeksterCache: Map<Tekst, String> = emptyMap()
    private val reporter = SelftestGenerator.Reporter(name = "SkrivestotteService", critical = false)
    private val retry = Retry(
        Retry.Config(
            initDelay = 30.seconds,
            growthFactor = 2.0,
            delayLimit = 1.hours,
        )
    )
    
    init {
        fixedRateTimer(name ="Prepopuler cache skrivestøtte", daemon = true, period = 1.hours.inWholeMilliseconds, initialDelay = 0) {
            runBlocking {
                reporter.ping {
                    prepopulerCache()
                }
            }
        }
    }
    
    fun hentTeksterFraSok(sokeVerdi: String): List<Tekst> {
        val alleTekster = teksterCache.values
        
        val sokeOrd = sokeVerdi
            .split(' ')
            .map { it.lowercase() }
            .filter { it.isNotBlank() }
            
        
        return alleTekster.filter { tekst ->
            sokeOrd.all { sokbareTeksterCache[tekst]?.contains(it) ?: false }
        }
        
    }
    
    fun hentTekstFraId(tekstId: UUID): Tekst? {
        return teksterCache[tekstId]
    }
    
    private suspend fun hentTekster(): Tekster {
        return skrivestotteClient.hentTekster()
    }
    
    private suspend fun prepopulerCache() {
        retry.run {
            teksterCache = hentTekster()
            sokbareTeksterCache = byggSokbareTekster(teksterCache.values)
        }
    }
    
    private fun byggSokbareTekster(tekster: Collection<Tekst>) : Map<Tekst, String> {
        return tekster.associateWith {
            listOf(
                it.overskrift,
                it.innhold.kombinert
            )
                .joinToString("\u0000")
                .lowercase()
        }
    }
}
