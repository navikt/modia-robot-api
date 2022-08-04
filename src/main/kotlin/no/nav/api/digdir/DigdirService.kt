package no.nav.api.digdir

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import no.nav.personoversikt.utils.SelftestGenerator
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes

class DigdirService (private val digdirClient: DigdirClient) {
    
    private val reporter = SelftestGenerator.Reporter(name = "DigdirService", critical = false)
    
    init {
        fixedRateTimer(name = "Ping digdir-krr-proxy", daemon = true, period = 1.minutes.inWholeMilliseconds, initialDelay = 0) {
            runBlocking {
                reporter.ping {
                    digdirClient.ping()
                }
            }
        }
    }
    
    @Serializable
    data class Epost(
        val verdi: String? = null,
        val sistOppdatert: Instant? = null,
        val sistVerifisert: Instant? = null,
    )
    
    suspend fun hentEpost(fnr: String): Epost {
        val krrData = digdirClient.hentKrrData(fnr)
        return Epost(
            verdi = krrData.epostadresse,
            sistOppdatert = krrData.epostadresseOppdatert,
            sistVerifisert = krrData.epostadresseVerifisert
        )
    }
}