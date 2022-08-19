package no.nav.api.dialog.saf

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import no.nav.api.generated.saf.HentBrukerssaker
import no.nav.api.generated.saf.enums.BrukerIdType
import no.nav.api.generated.saf.inputs.BrukerIdInput
import no.nav.utils.*
import java.net.URL

class SafClient(
    private val safUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create()
) {
    private val graphqlKtorClient = LoggingGraphQLKtorClient(
        name = "SAF",
        critical = false,
        url = URL(safUrl),
        httpClient = HttpClient(httpEngine)
    )

    suspend fun hentBrukersSaker(fnr: String): GraphQLClientResponse<HentBrukerssaker.Result> {
        return externalServiceCall {
            graphqlKtorClient.execute(
                request = HentBrukerssaker(
                    HentBrukerssaker.Variables(
                        BrukerIdInput(
                            id = fnr,
                            type = BrukerIdType.FNR
                        )
                    )
                ),
                requestCustomizer = {
                    val token = tokenclient.createMachineToMachineToken()
                    header("Authorization", "Bearer $token")
                    header("X-Correlation-ID", getCallId())
                    header("Content-Type", "application/json")
                }
            )
        }
    }
}