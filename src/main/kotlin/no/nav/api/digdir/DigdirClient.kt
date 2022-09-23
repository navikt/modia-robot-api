package no.nav.api.digdir

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import no.nav.utils.*

class DigdirClient(
    private val digdirKrrProxyUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
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
                    tokenclient.createMachineToMachineToken()
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
