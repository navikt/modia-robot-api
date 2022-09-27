package no.nav.api.dialog.sf

import no.nav.api.dialog.DialogService.*

class SFService(
    private val sfClient: SFClient,
) {
    suspend fun sendSporsmal(request: SfMeldingRequest, ident: String): Response {
        val nyHenvendelse = sfClient.sendSporsmal(request, ident)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun sendInfomelding(request: SfMeldingRequest, ident: String): Response {
        val nyHenvendelse = sfClient.sendInfomelding(request, ident)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun journalforMelding(request: JournalforRequest, ident: String) {
        sfClient.journalforMelding(request, ident)
    }

    suspend fun lukkTraad(kjedeId: String) {
        sfClient.lukkTraad(kjedeId)
    }
}
