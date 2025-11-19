package no.nav.api.utbetalinger

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.plugins.WebStatusException
import no.nav.utils.*

class UtbetalingerClient(
    private val utbetalingerUrl: String,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
    httpEngine: HttpClientEngine = lagHttpEngine(),
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
        RETTIGHETSHAVER,
        UTBETALT_TIL,
    }

    @Serializable
    data class Periode(
        val fom: String,
        val tom: String,
    )

    @Serializable
    enum class PeriodeType {
        UTBETALINGSPERIODE,
        YTELSESPERIODE,
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

    private val client =
        HttpClient(httpEngine) {
            installContentNegotiationAndIgnoreUnknownKeys()
            expectSuccess = false
        }

    suspend fun hentUtbetalinger(
        fnr: String,
        fra: LocalDate,
        til: LocalDate,
        token: String,
    ): List<Utbetaling> =
        externalServiceCall {
            val request: UtbetaldataRequest = lagUtbetaldataRequest(fnr, fra, til)
            val response =
                client.post("$utbetalingerUrl/v2/hent-utbetalingsinformasjon/intern") {
                    header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            when (response.status) {
                HttpStatusCode.NotFound -> {
                    emptyList()
                }

                HttpStatusCode.OK -> {
                    response
                        .runCatching { body<List<Utbetaling>>() }
                        .onFailure {
                            TjenestekallLogger.error(
                                "Feil ved deseralisering av json",
                                mapOf("exception" to it),
                            )
                        }.getOrThrow()
                }

                else -> {
                    throw WebStatusException(
                        message =
                            """
                            Henting av utbetalinger for bruker med fnr $fnr mellom $fra og $til feilet.
                            HttpStatus: ${response.status}
                            Body: ${response.bodyAsText()}
                            """.trimIndent(),
                        status = HttpStatusCode.InternalServerError,
                    )
                }
            }
        }

    private fun lagUtbetaldataRequest(
        fnr: String,
        fra: LocalDate,
        til: LocalDate,
    ) = UtbetaldataRequest(
        ident = fnr,
        rolle = Rolle.RETTIGHETSHAVER,
        periode =
            Periode(
                fom = fra.toString(),
                tom = til.toString(),
            ),
        periodetype = PeriodeType.UTBETALINGSPERIODE,
    )

    companion object {
        fun lagHttpEngine(): HttpClientEngine =
            OkHttp.create {
                addInterceptor(XCorrelationIdInterceptor())
                addInterceptor(
                    LoggingInterceptor(
                        name = "utbetaldata-sokos",
                        callIdExtractor = { getCallId() },
                    ),
                )
                addInterceptor(
                    HeadersInterceptor {
                        mapOf(
                            "nav-call-id" to getCallId(),
                        )
                    },
                )
            }
    }
}
