package no.nav.api.skrivestotte

import kotlinx.serialization.Serializable
import java.util.*

class SkrivestotteService (private val skrivestotteClient: SkrivestotteClient) {
    
    @Serializable
    data class Tekst(
        val tittel: String,
        val innhold: Map<Locale, String>,
    )
    
    suspend fun hentTeksterFraSok(sokeVerdi: String): List<Tekst> {
        val teksterMap = skrivestotteClient.hentTekster()
        val alleTekster = teksterMap.values.map { it.tilTekst() }
        
        val sokeOrd = sokeVerdi
            .split(' ')
            .map { it.lowercase() }
            .filter { it.isNotBlank() }
            
        
        return alleTekster.filter { tekst ->
            val matchTekst = listOf(
                tekst.tittel,
                tekst.innhold.values.joinToString("\u0000")
            )
                .joinToString("\u0000")
                .lowercase()
            sokeOrd.all { matchTekst.contains(it) }
        }
        
    }
    
    suspend fun hentTekstFraId(tekstId: UUID): Tekst {
        TODO()
    }
}

private fun SkrivestotteClient.Tekst.tilTekst(): SkrivestotteService.Tekst {
    return SkrivestotteService.Tekst(
        tittel = this.overskrift,
        innhold = this.innhold
    )
}
