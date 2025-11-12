package no.nav.api.kodeverk

import kotlinx.coroutines.runBlocking
import no.nav.api.generated.kodeverk.models.GetKodeverkKoderBetydningerResponse
import no.nav.personoversikt.common.utils.Retry
import no.nav.personoversikt.common.utils.SelftestGenerator
import no.nav.utils.TjenestekallLogger
import kotlin.collections.set
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

enum class KodeverkNavn(
    val kodeverkString: String,
) {
    POSTNUMMER("Postnummer"),
    LAND("Landkoder"),
    BYDEL("Bydeler"),
    KOMMUNE("Kommuner"),
}

class KodeverkService(
    private val kodeverkClient: KodeverkClient,
) {
    private var kodeverkCache: MutableMap<KodeverkNavn, Map<String, String>> = mutableMapOf()
    private val emptyKodeverk: Map<String, String> = emptyMap()
    private val reporter = SelftestGenerator.Reporter(name = "KodeverkService", critical = false)
    private val retry =
        Retry(
            Retry.Config(
                initDelay = 30.seconds,
                growthFactor = 2.0,
                delayLimit = 1.hours,
            ),
        )

    init {
        fixedRateTimer(
            name = "Prepopuler cache kodeverk",
            daemon = true,
            period = 1.hours.inWholeMilliseconds,
            initialDelay = 0,
        ) {
            runBlocking {
                reporter.ping {
                    prepopulerCache()
                }
            }
        }
    }

    private fun hentKodeverk(navn: KodeverkNavn): Map<String, String> = (kodeverkCache[navn] ?: emptyKodeverk)

    internal fun parseTilKodeverk(respons: GetKodeverkKoderBetydningerResponse): Map<String, String> {
        val res =
            respons.betydninger.mapValues { entry ->
                entry.value
                    .first()
                    .beskrivelser["nb"]
                    ?.term ?: entry.key
            }
        return res
    }

    fun hentKodeBeskrivelse(
        kodeverkNavn: KodeverkNavn,
        kodeRef: String,
        default: String,
    ): String {
        val kodeverk = this.hentKodeverk(kodeverkNavn)
        val beskrivelse = kodeverk[kodeRef] ?: return default
        return beskrivelse
    }

    internal fun prepopulerCache() {
        KodeverkNavn.entries.forEach { navn ->
            runBlocking {
                try {
                    retry.run {
                        kodeverkCache[navn] = parseTilKodeverk(kodeverkClient.hentKodeverkRaw(navn.kodeverkString))
                    }
                    TjenestekallLogger.info("${navn.kodeverkString}: kodeverk cachet", mapOf("antallKoder" to kodeverkCache[navn]?.size))
                } catch (e: Exception) {
                    TjenestekallLogger.error("kodeverk cache feilet: ${navn.kodeverkString}", mapOf("error" to e.message))
                }
            }
        }
    }
}
