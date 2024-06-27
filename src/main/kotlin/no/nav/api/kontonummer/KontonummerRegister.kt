package no.nav.api.kontonummer

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.nav.utils.*

class KontonummerRegister(
    private val kontonummerRegisterUrl: String,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
) {
    @Serializable
    data class Kontonummer(
        val kontonummer: String?,
    )

    @Serializable
    data class HentAktivKontoDTO(
        val kontohaver: String,
    )

    private val client =
        HttpClient(OkHttp) {
            installContentNegotiationAndIgnoreUnknownKeys()
            engine {
                addInterceptor(XCorrelationIdInterceptor())
                addInterceptor(
                    LoggingInterceptor(
                        name = "kontoregister",
                        callIdExtractor = { getCallId() },
                    ),
                )
            }
        }

    suspend fun hentKontonummer(
        fnr: String,
        ident: String,
        token: String,
    ): Kontonummer =
        externalServiceCall {
            client.post("$kontonummerRegisterUrl/rest/v1/hent-aktiv-konto") {
                headers {
                    append("Nav-Ident", ident)
                    append("Nav-Call-Id", getCallId())
                }
                header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
                contentType(ContentType.Application.Json)
                setBody(HentAktivKontoDTO(fnr))
            }.body()
        }
}
