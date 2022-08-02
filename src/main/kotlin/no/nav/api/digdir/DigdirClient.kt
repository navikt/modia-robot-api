package no.nav.api.digdir

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.utils.*

class DigdirClient(
    private val digdirKrrProxyUrl: String,
    private val tokenclient: MachineToMachineTokenClient,
) {
    
    @Serializable
    data class KrrData(
        val personident: String,
        val aktiv: Boolean,
        val kanVarsles: Boolean?,
        val reservert: Boolean?,
        val epostadresse: String?,
        val epostadresseOppdatert: LocalDateTime?,
        val epostadresseVerifisert: LocalDateTime?,
    )
    
    private val digdirApi = DownstreamApi(
        cluster = "prod-gcp",
        namespace = "team-rocket",
        application = "digdir-krr-proxy"
    )
    
    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        engine {
            addInterceptor(XCorrelationIdInterceptor())
            addInterceptor(
                LoggingInterceptor(
                    name = "digdir-krr-proxy",
                    callIdExtractor = { getCallId() }
                )
            )
            addInterceptor(
                AuthorizationInterceptor {
                    tokenclient.createMachineToMachineToken(digdirApi)
                }
            )
        }
    }
    
    suspend fun hentKrrData(fnr: String): KrrData = externalServiceCall {
        client.get("$digdirKrrProxyUrl/rest/v1/person") {
            headers {
                append("Nav-Personident", fnr)
                append("Nav-Call-Id", getCallId())
            }
        }
    }
    
    suspend fun ping() = externalServiceCall {
        client.get<HttpResponse>("$digdirKrrProxyUrl/rest/ping").status
    }

}