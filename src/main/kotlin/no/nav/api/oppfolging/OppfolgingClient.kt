package no.nav.api.oppfolging

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.nav.utils.*

class OppfolgingClient(
    private val oppfolgingUrl: String,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
) {
    @Serializable
    class Status(
        val erUnderOppfolging: Boolean?,
    )

    @Serializable
    class VeilederIdent(
        val veilederIdent: String?,
    )

    private val client =
        HttpClient(OkHttp) {
            installContentNegotiationAndIgnoreUnknownKeys()
            engine {
                addInterceptor(XCorrelationIdInterceptor())
                addInterceptor(
                    LoggingInterceptor(
                        name = "veilarboppfolging",
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

    suspend fun hentOppfolgingStatus(
        fnr: String,
        token: String,
    ): Status =
        externalServiceCall {
            client
                .get("$oppfolgingUrl/v2/oppfolging?fnr=$fnr") {
                    header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
                }.body()
        }

    suspend fun hentOppfolgingVeileder(
        fnr: String,
        token: String,
    ): VeilederIdent? =
        externalServiceCall {
            val response =
                client.get("$oppfolgingUrl/v2/veileder?fnr=$fnr") {
                    header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
                }
            when (response.status) {
                HttpStatusCode.NoContent -> null
                HttpStatusCode.OK -> response.body()
                else -> error("Ukjent status code: ${response.status}")
            }
        }
}
