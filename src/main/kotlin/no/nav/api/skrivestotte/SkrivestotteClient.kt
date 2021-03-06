package no.nav.api.skrivestotte

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import no.nav.utils.*
import java.util.*

typealias Tekster = Map<UUID, SkrivestotteClient.Tekst>

class SkrivestotteClient(
    val skrivestotteUrl: String
) {
    @Serializable
    data class Tekst(
        @Contextual
        val id: UUID?,
        val overskrift: String,
        val tags: List<String>,
        val innhold: Innhold,
        val vekttall: Int = 0
    )
    
    @Serializable
    data class Innhold(
        val nb_NO: String? = null,
        val nn_NO: String? = null,
        val en_US: String? = null,
        val se_NO: String? = null,
        val de_DE: String? = null,
        val fr_FR: String? = null,
        val es_ES: String? = null,
        val pl_PL: String? = null,
        val ru_RU: String? = null,
        val ur: String? = null
    ) {
        fun kombinert() = listOfNotNull(nb_NO, nn_NO, en_US, se_NO, de_DE, fr_FR, es_ES, pl_PL, ru_RU, ur).joinToString("\u0000")
    }

    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(jsonSerializer)
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