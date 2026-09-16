package no.nav.api.digdir

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import no.nav.plugins.WebStatusException
import no.nav.utils.*

class DigdirClient(
    private val digdirKrrProxyUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
) {
    @Serializable
    data class KrrData(
        val personident: String? = null,
        val aktiv: Boolean? = null,
        val kanVarsles: Boolean? = null,
        val reservert: Boolean? = null,
        val epostadresse: String? = null,
        val epostadresseOppdatert: Instant? = null,
        val epostadresseVerifisert: Instant? = null,
        val mobiltelefonnummer: String? = null,
        val mobiltelefonnummerOppdatert: Instant? = null,
        val mobiltelefonnummerVerifisert: Instant? = null,
    )

    @Serializable
    private data class PersonerRequest(
        val personidenter: Set<String>,
    )

    @Serializable
    private data class PersonerResponse(
        val personer: Map<String, KrrData> = emptyMap(),
        val feil: Map<String, String> = emptyMap(),
    )

    private val client =
        HttpClient(OkHttp) {
            installContentNegotiationAndIgnoreUnknownKeys()
            engine {
                addInterceptor(XCorrelationIdInterceptor())
                addInterceptor(
                    LoggingInterceptor(
                        name = "digdir-krr-proxy",
                        callIdExtractor = { getCallId() },
                    ),
                )
            }
        }

    suspend fun hentKrrData(
        fnr: String,
        token: String,
    ): KrrData =
        externalServiceCall {
            val response =
                client.post("$digdirKrrProxyUrl/rest/v1/personer") {
                    headers {
                        append("Nav-Call-Id", getCallId())
                    }
                    header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
                    contentType(ContentType.Application.Json)
                    setBody(PersonerRequest(personidenter = setOf(fnr)))
                }
            when (response.status) {
                HttpStatusCode.OK -> response.body<PersonerResponse>().kontaktinformasjonFor(fnr)
                HttpStatusCode.Forbidden ->
                    throw WebStatusException(
                        "Ikke tilgang til brukers kontaktinformasjon i KRR",
                        HttpStatusCode.Forbidden,
                    )
                else -> error("Ukjent status code: ${response.status}")
            }
        }

    /**
     * Proxyen svarer 200 også når den ikke fant personen. Da ligger årsaken i `feil`, og vi må
     * oversette den selv i stedet for å late som om brukeren mangler kontaktinformasjon.
     */
    private fun PersonerResponse.kontaktinformasjonFor(fnr: String): KrrData {
        personer[fnr]?.let { return it }

        val arsak = feil[fnr]
        if (arsak == "person_ikke_funnet") {
            throw WebStatusException("Fant ingen person med oppgitt ident i KRR", HttpStatusCode.NotFound)
        }
        error("Fikk verken kontaktinformasjon eller kjent feilkode fra digdir-krr-proxy: $arsak")
    }

    suspend fun ping() =
        externalServiceCall {
            client
                .get("$digdirKrrProxyUrl/rest/ping") {
                    header("Authorization", "Bearer ${tokenclient.createMachineToMachineToken()}")
                }.status
        }
}
