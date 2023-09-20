package no.nav.api.dialog.saf

import no.nav.api.generated.saf.hentbrukerssaker.Sak

class SafService(
    private val safClient: SafClient,
) {
    suspend fun hentBrukersSaker(
        fnr: String,
        token: String,
    ): List<Sak> {
        return safClient
            .hentBrukersSaker(fnr, token)
            .data
            ?.saker
            ?.filterNotNull()
            ?: emptyList()
    }
}
