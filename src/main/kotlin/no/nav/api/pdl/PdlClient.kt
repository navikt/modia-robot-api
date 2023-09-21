package no.nav.api.pdl

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import no.nav.api.generated.pdl.HentAktorid
import no.nav.api.generated.pdl.HentNavn
import no.nav.api.generated.pdl.HentPersonalia
import no.nav.utils.*
import java.net.URL

class PdlClient(
    private val pdlUrl: String,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create(),
) {
    private val graphqlClient =
        LoggingGraphQLKtorClient(
            name = "PDL",
            critical = false,
            url = URL(pdlUrl),
            httpClient = HttpClient(httpEngine),
        )

    private fun requestConfig(token: String): HeadersBuilder =
        {
            val oboToken = oboTokenProvider.exchangeOnBehalfOfToken(token)
            header("Authorization", "Bearer $oboToken")
            header("Tema", "GEN")
            header("X-Correlation-ID", getCallId())
        }

    suspend fun hentPersonalia(
        fnr: String,
        token: String,
    ): GraphQLClientResponse<HentPersonalia.Result> {
        return externalServiceCall {
            graphqlClient.execute(
                request = HentPersonalia(HentPersonalia.Variables(fnr)),
                requestCustomizer = requestConfig(token),
            )
        }
    }

    suspend fun hentAktorid(
        fnr: String,
        token: String,
    ): GraphQLClientResponse<HentAktorid.Result> {
        return externalServiceCall {
            graphqlClient.execute(
                request =
                    HentAktorid(
                        HentAktorid.Variables(
                            ident = fnr,
                        ),
                    ),
                requestCustomizer = requestConfig(token),
            )
        }
    }

    suspend fun hentNavn(
        fnr: String,
        token: String,
    ): GraphQLClientResponse<HentNavn.Result> {
        return externalServiceCall {
            graphqlClient.execute(
                request = HentNavn(HentNavn.Variables(fnr)),
                requestCustomizer = requestConfig(token),
            )
        }
    }
}
