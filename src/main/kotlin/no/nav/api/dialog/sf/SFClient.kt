package no.nav.api.dialog.sf

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.nav.api.dialog.DialogService.*
import no.nav.utils.*

class SFClient(
    private val sfUrl: String,
    private val oboTokenProvider: BoundedOnBehalfOfTokenClient,
) {
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
                    name = "sf-henvendelse-api",
                    callIdExtractor = { getCallId() }
                )
            )
        }
    }

    @Serializable
    data class HenvendelseDTO(val kjedeId: String)

    suspend fun sendSporsmal(request: SfMeldingRequest, ident: String, token: String): HenvendelseDTO = externalServiceCall {
        client.post("$sfUrl/henvendelse/ny/melding") {
            headers {
                append("Nav-Ident", ident)
            }
            header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
            contentType(ContentType.Application.Json)
            body = request
        }
    }

    suspend fun sendInfomelding(request: SfMeldingRequest, ident: String, token: String): HenvendelseDTO = externalServiceCall {
        client.post("$sfUrl/henvendelse/ny/melding") {
            headers {
                append("Nav-Ident", ident)
            }
            header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
            contentType(ContentType.Application.Json)
            body = request
        }
    }

    suspend fun journalforMelding(request: JournalforRequest, ident: String, token: String): Unit = externalServiceCall {
        client.post("$sfUrl/henvendelse/journal") {
            headers {
                append("Nav-Ident", ident)
            }
            header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
            contentType(ContentType.Application.Json)
            body = request
        }
    }

    suspend fun lukkTraad(kjedeId: String, token: String): Unit = externalServiceCall {
        client.post("$sfUrl/henvendelse/meldingskjede/lukk?kjedeId=$kjedeId") {
            header("Authorization", "Bearer ${oboTokenProvider.exchangeOnBehalfOfToken(token)}")
        }
    }
}
