package no.nav.api.oppfolging

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import no.nav.utils.*

class OppfolgingClient(
    private val oppfolgingUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
) {
    @Serializable
    class Status(val erUnderOppfolging: Boolean?)

    @Serializable
    class VeilederIdent(val veilederIdent: NavIdent?)
    
    @Serializable
    data class NavIdent(
        val id: String
    )

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
        }
    }

    suspend fun hentOppfolgingStatus(fnr: String): Status = externalServiceCall {
        client.get("$oppfolgingUrl/v2/oppfolging?fnr=$fnr")
    }

    suspend fun hentOppfolgingVeileder(fnr: String): VeilederIdent? = externalServiceCall {
        client.get("$oppfolgingUrl/v2/veileder?fnr=$fnr")
    }
}
