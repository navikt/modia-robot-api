package no.nav.api.skrivestotte

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import no.nav.utils.*
import java.util.*

enum class Locale {
    nb_NO, nn_NO, en_US, se_NO, de_DE, fr_FR, es_ES, pl_PL, ru_RU, ur
}

typealias Tekster = Map<UUID, SkrivestotteClient.Tekst>

class SkrivestotteClient(
    val skrivestotteUrl: String
) {

    @Serializable
    data class Tekst(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID?,
        val overskrift: String,
        val tags: List<String>,
        val innhold: Map<Locale, String>,
        val vekttall: Int = 0
    )

    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            addInterceptor(XCorrelationIdInterceptor())
            addInterceptor(
                LoggingInterceptor(
                    name = "skrivestotte",
                    callIdExtractor = { getCallId() }
                )
            )
        }
    }

    suspend fun hentTekster(): Tekster = externalServiceCall {
        client.get("$skrivestotteUrl/skrivestotte")
    }
}
