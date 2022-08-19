package no.nav.api.pdl

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import no.nav.api.generated.pdl.HentPersonalia
import no.nav.api.generated.pdl.HentAktorid
import no.nav.api.generated.pdl.HentNavn
import no.nav.utils.*
import java.net.URL

class PdlClient(
    private val pdlUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    httpEngine: HttpClientEngine = OkHttp.create()
) {
    private val graphqlKtorClient = LoggingGraphQLKtorClient(
        name = "PDL",
        critical = false,
        url = URL(pdlUrl),
        httpClient = HttpClient(httpEngine)
    )

    private val requestConfig: HeadersBuilder = {
        val token = tokenclient.createMachineToMachineToken()
        header("Nav-Consumer-Token", "Bearer $token")
        header("Authorization", "Bearer $token")
        header("Tema", "GEN")
        header("X-Correlation-ID", getCallId())
    }

    suspend fun hentPersonalia(fnr: String): GraphQLClientResponse<HentPersonalia.Result> {
        return externalServiceCall {
            graphqlKtorClient.execute(
                request = HentPersonalia(HentPersonalia.Variables(fnr)),
                requestCustomizer = requestConfig
            )
        }
    }

    suspend fun hentAktorid(fnr: String): GraphQLClientResponse<HentAktorid.Result> {
        return externalServiceCall {
            graphqlKtorClient.execute(
                request = HentAktorid(
                    HentAktorid.Variables(
                        ident = fnr
                    )
                ),
                requestCustomizer = requestConfig
            )
        }
    }
    
    suspend fun hentNavn(fnr: String): GraphQLClientResponse<HentNavn.Result> {
        return externalServiceCall {
            graphqlKtorClient.execute(
                request = HentNavn(HentNavn.Variables(fnr)),
                requestCustomizer = requestConfig
            )
        }
    }
    
    
}