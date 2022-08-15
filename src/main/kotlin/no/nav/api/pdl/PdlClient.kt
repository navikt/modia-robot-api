package no.nav.api.pdl

import HentAktorid
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import no.nav.api.pdl.queries.HentPersonalia
import no.nav.utils.*

class PdlClient(
    private val pdlUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create()
) {
    private val graphqlClient = GraphQLClient(
        httpClient = GraphQLClient.createHttpClient(httpEngine),
        config = GraphQLClientConfig(
            serviceName = "PDL",
            critical = false,
            requestConfig = {
                val token = tokenclient.createMachineToMachineToken()
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

    suspend fun hentAktorid(fnr: String): GraphQLResponse<HentAktorid.Result> {
        return externalServiceCall {
            graphqlClient.execute(
                HentAktorid(HentAktorid.Variables(fnr))
            )
        }
    }
}