package no.nav.api.utbetalinger

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.plugins.WebStatusException
import no.nav.utils.*

class UtbetalingerClient(
    private val utbetalingerUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
    httpEngine: HttpClientEngine = lagHttpEngine(tokenclient),
) {

    @Serializable
    data class UtbetaldataRequest(
        val ident: String,
        val rolle: Rolle,
        val periode: Periode,
        val periodetype: PeriodeType,
    )

    @Serializable
    enum class Rolle {
        RETTIGHETSHAVER, UTBETALT_TIL
    }

    @Serializable
    data class Periode(
        val fom: String,
        val tom: String,
    )

    @Serializable
    enum class PeriodeType {
        UTBETALINGSPERIODE, YTELSESPERIODE
    }

    @Serializable
    class Utbetaling(
        val utbetalingsstatus: String,
        val ytelseListe: List<Ytelse>,
    )

    @Serializable
    class Ytelse(
        val ytelsestype: String? = null,
        val ytelsesperiode: Periode,
        val ytelseskomponentListe: List<YtelseKomponent>? = null,
    )

    @Serializable
    class YtelseKomponent(
        val ytelseskomponenttype: String,
    )

    private val client = HttpClient(httpEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        expectSuccess = false
    }

    suspend fun hentUtbetalinger(fnr: String, fra: LocalDate, til: LocalDate): List<Utbetaling> = externalServiceCall {
        val request: UtbetaldataRequest = lagUtbetaldataRequest(fnr, fra, til)
        val response = client.post<HttpResponse>("$utbetalingerUrl/v2/hent-utbetalingsinformasjon/intern") {
            contentType(ContentType.Application.Json)
            body = request
        }

        when (response.status) {
            HttpStatusCode.NotFound -> emptyList()
            HttpStatusCode.OK ->
                response
                    .runCatching { receive<List<Utbetaling>>() }
                    .onFailure { TjenestekallLogger.error("Feil ved deseralisering av json", mapOf("exception" to it)) }
                    .getOrThrow()
            else -> throw WebStatusException(
                message = """
                    Henting av utbetalinger for bruker med fnr $fnr mellom $fra og $til feilet.
                    HttpStatus: ${response.status}
                    Body: ${response.readText()}
                """.trimIndent(),
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    private fun lagUtbetaldataRequest(
        fnr: String,
        fra: LocalDate,
        til: LocalDate,
    ) = UtbetaldataRequest(
        ident = fnr,
        rolle = Rolle.RETTIGHETSHAVER,
        periode = Periode(
            fom = fra.toString(),
            tom = til.toString()
        ),
        periodetype = PeriodeType.UTBETALINGSPERIODE
    )

    companion object {
        fun lagHttpEngine(tokenclient: BoundedMachineToMachineTokenClient): HttpClientEngine {
            return OkHttp.create {
                addInterceptor(XCorrelationIdInterceptor())
                addInterceptor(
                    LoggingInterceptor(
                        name = "utbetaldata-sokos",
                        callIdExtractor = { getCallId() }
                    )
                )
                addInterceptor(
                    AuthorizationInterceptor {
                        tokenclient.createMachineToMachineToken()
                    }
                )
                addInterceptor(
                    HeadersInterceptor {
                        mapOf(
                            "nav-call-id" to getCallId()
                        )
                    }
                )
            }
        }
    }
}
