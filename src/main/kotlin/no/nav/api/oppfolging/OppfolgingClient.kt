package no.nav.api.oppfolging

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.utils.*

class OppfolgingClient(
    private val oppfolgingUrl: String,
    private val tokenclient: MachineToMachineTokenClient,
) {
    private val oppfolgingApi = DownstreamApi(
        cluster = "prod-fss",
        namespace = "pto",
        application = "veilarboppfolging"
    )

    @Serializable
    class Status(val underOppfolging: Boolean?)

    @Serializable
    class VeilederId(val veilederId: String?)

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
                    tokenclient.createMachineToMachineToken(oppfolgingApi)
                }
            )
        }
    }

    suspend fun hentOppfolgingStatus(fnr: String): Status = externalServiceCall {
        client.get("$oppfolgingUrl/oppfolging?fnr=$fnr")
    }

    suspend fun hentOppfolgingVeileder(fnr: String): VeilederId? = externalServiceCall {
        client.get("$oppfolgingUrl/person/$fnr/oppfolgingsstatus")
    }
}
