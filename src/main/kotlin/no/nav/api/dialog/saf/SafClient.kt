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
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create(),
) {
    private val graphqlClient = LoggingGraphQLKtorClient(
        name = "SAF",
        critical = false,
        url = URL(safUrl),
        httpClient = HttpClient(httpEngine),
    )

    suspend fun hentBrukersSaker(fnr: String, token: String): GraphQLClientResponse<HentBrukerssaker.Result> {
        return externalServiceCall {
            graphqlClient.execute(
                request = HentBrukerssaker(
                    HentBrukerssaker.Variables(
                        BrukerIdInput(
                            id = fnr,
                            type = BrukerIdType.FNR,
                        ),
                    ),
                ),
                requestCustomizer = {
                    val oboToken = oboTokenProvider.exchangeOnBehalfOfToken(token)
                    header("Authorization", "Bearer $oboToken")
                    header("X-Correlation-ID", getCallId())
                },
            )
        }
    }
}
