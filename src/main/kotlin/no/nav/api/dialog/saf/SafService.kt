package no.nav.api.dialog.saf

import no.nav.api.dialog.saf.queries.HentBrukerssaker

class SafService(
    private val safClient: SafClient
) {
    suspend fun hentBrukersSaker(fnr: String): List<HentBrukerssaker.Sak> {
        return safClient
            .hentBrukersSaker(fnr)
            .data
            ?.saker
            ?.filterNotNull()
            ?: emptyList()
    }
}