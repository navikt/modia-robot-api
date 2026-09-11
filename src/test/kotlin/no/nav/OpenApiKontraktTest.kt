package no.nav

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import no.nav.mock.MockConsumers
import no.nav.mock.MockEnv
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Låser OpenAPI-spesifikasjonen mot en versjonert fil, slik at enhver endring i API-kontrakten
 * blir synlig i en pull request-diff i stedet for å oppdages av konsumentene i produksjon.
 *
 * Spesifikasjonen bygges av `NotarizedRoute`-installene ute i rutene. En endret `responseType`,
 * en fjernet parameter eller en ny rute vil derfor slå ut her.
 *
 */
class OpenApiKontraktTest {
    @Test
    fun `api-kontrakten er uendret`() {
        val gjeldende = hentSpesifikasjon()

        if (skalOppdatereSnapshot) {
            snapshotFil.parentFile.mkdirs()
            snapshotFil.writeText(gjeldende)
            return
        }

        if (!snapshotFil.exists()) {
            snapshotFil.parentFile.mkdirs()
            snapshotFil.writeText(gjeldende)
            error(
                "Fant ingen kontraktsfil på $SNAPSHOT_STI, så den ble opprettet nå. " +
                    "Les gjennom innholdet og commit den.",
            )
        }

        assertEquals(
            snapshotFil.readText().normalisertJson(),
            gjeldende,
            "API-kontrakten har endret seg. Er endringen tilsiktet, kjør " +
                "`./gradlew test -PoppdaterOpenapi=true` og gå gjennom diffen i $SNAPSHOT_STI. ",
        )
    }

    /**
     * Starter hele applikasjonsmodulen, ikke bare OpenAPI-pluginen, slik at spesifikasjonen
     * som testes er den samme som deployes.
     */
    private fun hentSpesifikasjon(): String {
        lateinit var spesifikasjon: String
        testApplication {
            application {
                apiModule(
                    disableSecurity = true,
                    env = MockEnv,
                    consumers = MockConsumers,
                    services = ServicesImpl(MockConsumers),
                )
            }

            val response = client.get("/openapi.json")

            assertEquals(
                HttpStatusCode.OK,
                response.status,
                "Klarte ikke å hente /openapi.json",
            )
            spesifikasjon = response.bodyAsText().normalisertJson()
        }
        return spesifikasjon
    }

    /**
     * Spesifikasjonsruten svarer med kompakt JSON. Vi lagrer den formatert og med sorterte nøkler,
     * så diffen viser hva som faktisk endret seg og ikke bare én lang linje. Sorteringen gjør også
     * snapshotet robust mot at Kompendium bygger objektene i en annen rekkefølge mellom kjøringer.
     *
     * `info.version` følger image-taggen og endrer seg for hvert bygg. Den erstattes med en fast
     * verdi her, ellers ville snapshotet vært utdatert i det øyeblikket det ble committet.
     */
    private fun String.normalisertJson(): String {
        val sortert = Json.parseToJsonElement(this).sortert().utenVersjon()
        return snapshotFormat.encodeToString(JsonElement.serializer(), sortert)
    }

    /** Se [normalisertJson]. */
    private fun JsonElement.utenVersjon(): JsonElement {
        val rot = this as? JsonObject ?: return this
        val info = rot["info"] as? JsonObject ?: return this
        return JsonObject(
            rot + ("info" to JsonObject(info + ("version" to JsonPrimitive("<versjon>")))),
        )
    }

    /** Sorterer nøkler rekursivt. Rekkefølgen i lister er en del av kontrakten og beholdes. */
    private fun JsonElement.sortert(): JsonElement =
        when (this) {
            is JsonObject -> JsonObject(entries.sortedBy { it.key }.associate { it.key to it.value.sortert() })
            is JsonArray -> JsonArray(map { it.sortert() })
            else -> this
        }

    private companion object {
        const val SNAPSHOT_STI = "docs/openapi.json"

        val snapshotFil = File(SNAPSHOT_STI)

        val skalOppdatereSnapshot = System.getProperty("oppdaterOpenapi") == "true"

        val snapshotFormat = Json { prettyPrint = true }
    }
}
