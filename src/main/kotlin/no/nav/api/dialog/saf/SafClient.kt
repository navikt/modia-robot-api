package no.nav.api.dialog.saf

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import no.nav.api.dialog.saf.queries.HentBrukerssaker
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.personoversikt.utils.SelftestGenerator
import no.nav.utils.*

class SafClient(
    private val safUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create()
) {
    private val safApi = DownstreamApi(
        cluster = "prod-fss",
        namespace = "teamdokumenthandtering",
        application = "saf"
    )

    private val httpClient = HttpClient(httpEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    private val graphqlClient = GraphQLClient(
        httpClient = httpClient,
        config = GraphQLClientConfig(
            serviceName = "SAF",
            requestConfig = {
                val token = tokenclient.createMachineToMachineToken()
                url(safUrl)
                header("Authorization", "Bearer $token")
                header("X-Correlation-ID", getCallId())
                header("Content-Type", "application/json")
            }
        )
    )

    suspend fun hentBrukersSaker(fnr: String): GraphQLResponse<HentBrukerssaker.Result> {
        return externalServiceCall {
            graphqlClient.execute(
                HentBrukerssaker(
                    HentBrukerssaker.Variables(
                        HentBrukerssaker.BrukerIdInput(
                            id = fnr,
                            type = HentBrukerssaker.BrukerIdType.FNR
                        )
                    )
                )
            )
        }
    }
}