package no.nav.api.dialog.saf

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import no.nav.api.dialog.saf.queries.HentBrukerssaker
import no.nav.utils.*

class SafClient(
    private val safUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create()
) {
    private val graphqlClient = GraphQLClient(
        httpClient = GraphQLClient.createHttpClient(httpEngine),
        config = GraphQLClientConfig(
            serviceName = "SAF",
            critical = false,
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