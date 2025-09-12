package no.nav.api.kodeverk

import no.nav.mock.MockConsumers
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KodeverkServiceTest {
    private val kodeverkService: KodeverkService = KodeverkService(MockConsumers.kodeverkClient)

    @Test
    fun `skal populere kodeverk cache`() {
        kodeverkService.prepopulerCache()
        val landkode = kodeverkService.hentKodeBeskrivelse(KodeverkNavn.LAND, "NO", "NO")
        val poststed = kodeverkService.hentKodeBeskrivelse(KodeverkNavn.POSTNUMMER, "0660", "0660")

        assertEquals("Oslo", poststed)
        assertEquals("Norge", landkode)
    }
}
