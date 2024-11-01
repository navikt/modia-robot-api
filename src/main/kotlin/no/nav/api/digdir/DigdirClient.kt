package no.nav.api.digdir

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import no.nav.utils.*

class DigdirClient(
    private val digdirKrrProxyUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
) {
    @Serializable
    data class KrrData(
        val personident: String,
        val aktiv: Boolean,
        val kanVarsles: Boolean?,
        val reservert: Boolean?,
        val epostadresse: String?,
        val epostadresseOppdatert: Instant?,
        val epostadresseVerifisert: Instant?,
        val mobiltelefonnummer: String?,
        val mobiltelefonnummerOppdatert: Instant?,
        val mobiltelefonnummerVerifisert: Instant?,
    )

    private val client =
        HttpClient(OkHttp) {
            installContentNegotiationAndIgnoreUnknownKeys()
            engine {
                addInterceptor(XCorrelationIdInterceptor())
                addInterceptor(
                    LoggingInterceptor(
                        name = "digdir-krr-proxy",
                        callIdExtractor = { getCallId() },
                    ),
                )
            }
        }

    suspend fun hentKrrData(
        fnr: String,
        token: String,
    ): KrrData =
        externalServiceCall {
            client
                .get("$digdirKrrProxyUrl/rest/v1/person") {
                    headers {
                        append("Nav-Personident", fnr)
                        append("Nav-Call-Id", getCallId())
                    }
                    header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
                }.body()
        }

    suspend fun ping() =
        externalServiceCall {
            client
                .get("$digdirKrrProxyUrl/rest/ping") {
                    header("Authorization", "Bearer ${tokenclient.createMachineToMachineToken()}")
                }.status
        }
}
