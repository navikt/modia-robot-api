package no.nav.api.utbetalinger

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.plugins.WebStatusException
import no.nav.utils.*

class UtbetalingerClient(
    private val utbetalingerUrl: String,
    private val tokenclient: BoundedMachineToMachineTokenClient,
) {
    
    @Serializable
    data class UtbetaldataRequest(
        val ident: String,
        val rolle: Rolle,
        val periode: Periode,
        val periodetype: PeriodeType
    )
    
    @Serializable
    enum class Rolle {
        RETTIGHETSHAVER, UTBETALT_TIL
    }
    
    @Serializable
    data class Periode(
        val fom: String,
        val tom: String
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
        val ytelsestype: String?,
        val ytelsesperiode: Periode,
    )
    
    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        engine {
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
    
    suspend fun hentUtbetalinger(fnr: String, fra: LocalDate, til: LocalDate): List<Utbetaling> = externalServiceCall {
        val request: UtbetaldataRequest = lagUtbetaldataRequest(fnr, fra, til)
        try {
            client.post("$utbetalingerUrl/v1/hent-utbetalingsinformasjon/intern") {
                contentType(ContentType.Application.Json)
                body = request
            }
        } catch (ex: Exception) {
            throw WebStatusException(
                message = ex.message ?: "Henting av utbetalinger for bruker med fnr $fnr mellom $fra og $til feilet.",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
    
    private fun lagUtbetaldataRequest(
        fnr: String,
        fra: LocalDate,
        til: LocalDate
    ) = UtbetaldataRequest(
        ident = fnr,
        rolle = Rolle.RETTIGHETSHAVER,
        periode = Periode(
            fom = fra.toString(),
            tom = til.toString()
        ),
        periodetype = PeriodeType.UTBETALINGSPERIODE
    )
}

