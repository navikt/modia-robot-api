package no.nav.api.oppfolging

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.nav.utils.*

class OppfolgingClient(
    private val oppfolgingUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
) {
    @Serializable
    class Status(val erUnderOppfolging: Boolean?)

    @Serializable
    class VeilederIdent(val veilederIdent: String?)

    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            addInterceptor(XCorrelationIdInterceptor())
            addInterceptor(
                LoggingInterceptor(
                    name = "veilarboppfolging",
                    callIdExtractor = { getCallId() }
                )
            )
            addInterceptor(
                AuthorizationInterceptor {
                    tokenclient.createMachineToMachineToken()
                }
            )
            addInterceptor(
                HeadersInterceptor {
                    mapOf(
                        "Nav-Consumer-Id" to navConsumerId
                    )
                }
            )
        }
    }

    suspend fun hentOppfolgingStatus(fnr: String): Status = externalServiceCall {
        client.get("$oppfolgingUrl/v2/oppfolging?fnr=$fnr")
    }

    suspend fun hentOppfolgingVeileder(fnr: String): VeilederIdent? = externalServiceCall {
        val response = client.get<HttpResponse>("$oppfolgingUrl/v2/veileder?fnr=$fnr")
        when (response.status) {
            HttpStatusCode.NoContent -> null
            HttpStatusCode.OK -> response.receive()
            else -> error("Ukjent status code: ${response.status}")
        }
    }
}
