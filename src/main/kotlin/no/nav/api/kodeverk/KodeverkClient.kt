
package no.nav.api.kodeverk
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.*
import no.nav.api.generated.kodeverk.apis.KodeverkApi
import no.nav.api.generated.kodeverk.models.GetKodeverkKoderBetydningerResponse
import no.nav.utils.*

class KodeverkClient(
    kodeverkUrl: String,
    private val tokenClient: BoundedMachineToMachineTokenClient,
    httpEngine: HttpClientEngine =
        OkHttp.create {
            addInterceptor(XCorrelationIdInterceptor())
            addInterceptor(
                LoggingInterceptor(
                    name = "kodeverk",
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
            addInterceptor(
                AuthorizationInterceptor {
                    tokenClient.createMachineToMachineToken()
                },
            )
        },
) {
    private val api =
        KodeverkApi(kodeverkUrl, httpEngine) { config ->
            config.installContentNegotiationAndIgnoreUnknownKeys()
        }

    suspend fun hentKodeverkRaw(navn: String): GetKodeverkKoderBetydningerResponse =
        externalServiceCall {
            val response =
                api.betydningUsingGET(
                    navCallId = getCallId(),
                    navConsumerId = "modia-robot-api",
                    kodeverksnavn = navn,
                    spraak = listOf("nb"),
                    ekskluderUgyldige = null,
                    oppslagsdato = null,
                )
            when (response.success) {
                true -> response.body()
                else -> error("Feil ved henting av kodeverk: ${response.status}")
            }
        }
}
