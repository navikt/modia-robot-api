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
    data class Kontaktinformasjon(
        val epostadresse: String? = null,
        val epostadresseOppdatert: Instant? = null,
        val epostadresseVerifisert: Instant? = null,
        val mobiltelefonnummer: String? = null,
        val mobiltelefonnummerOppdatert: Instant? = null,
        val mobiltelefonnummerVerifisert: Instant? = null,
    )
    
    suspend fun hentKontaktinformasjon(fnr: String): Kontaktinformasjon {
        val krrData = digdirClient.hentKrrData(fnr)
        return Kontaktinformasjon(
            epostadresse = krrData.epostadresse,
            epostadresseOppdatert = krrData.epostadresseOppdatert,
            epostadresseVerifisert = krrData.epostadresseVerifisert,
            mobiltelefonnummer = krrData.mobiltelefonnummer,
            mobiltelefonnummerOppdatert = krrData.mobiltelefonnummerOppdatert,
            mobiltelefonnummerVerifisert = krrData.mobiltelefonnummerVerifisert
        )
    }
}