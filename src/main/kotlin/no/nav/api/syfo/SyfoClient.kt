package no.nav.api.syfo

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.nav.plugins.WebStatusException
import no.nav.utils.*

class SyfoClient(
    private val syfoUrl: String,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
) {
    @Serializable
    class PersonIdentValue(
        val value: String,
    )

    @Serializable
    class SyfoTildeling(
        val personident: PersonIdentValue,
        val tildeltVeilederident: String? = null,
        val tildeltEnhet: String? = null,
    )

    private val client =
        HttpClient(OkHttp) {
            installContentNegotiationAndIgnoreUnknownKeys()
            engine {
                addInterceptor(XCorrelationIdInterceptor())
                addInterceptor(
                    LoggingInterceptor(
                        name = "isyfooversikt",
                        callIdExtractor = { getCallId() },
                    ),
                )
                addInterceptor(
                    HeadersInterceptor {
                        mapOf(
                            "Nav-Consumer-Id" to navConsumerId,
                        )
                    },
                )
            }
        }

    suspend fun hentSyfoVeileder(
        fnr: String,
        token: String,
    ): SyfoTildeling? =
        externalServiceCall {
            val response =
                client.get("$syfoUrl/v2/persontildeling/personer/single") {
                    header("nav-personident", fnr)
                    header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
                }
            when (response.status) {
                HttpStatusCode.NoContent -> null
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.Forbidden -> throw WebStatusException("Ikke tilgang til brukers syfoveileder", HttpStatusCode.Forbidden)
                else -> error("Ukjent status code: ${response.status}")
            }
        }
}
