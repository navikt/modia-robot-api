package no.nav.api.pdl

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import no.nav.api.pdl.queries.HentPersonalia
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.utils.*

class PdlClient(
    private val pdlUrl: String,
    private val tokenclient: MachineToMachineTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create()
) {
    private val pdlApi = DownstreamApi(
        cluster = "prod-fss",
        namespace = "pdl",
        application = "pdl-api"
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
            serviceName = "PDL",
            requestConfig = {
                val token = tokenclient.createMachineToMachineToken(pdlApi)
                url(pdlUrl)
                header("Nav-Consumer-Token", "Bearer $token")
                header("Authorization", "Bearer $token")
                header("Tema", "GEN")
                header("X-Correlation-ID", getCallId())
            }
        )
    )

    suspend fun hentPersonalia(fnr: String): GraphQLResponse<HentPersonalia.Result> {
        return externalServiceCall {
            graphqlClient.execute(
                HentPersonalia(HentPersonalia.Variables(fnr))
            )
        }
    }
}