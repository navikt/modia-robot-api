package no.nav.api.dialog.sf

import no.nav.api.dialog.DialogService.*

class SFService(
    private val sfClient: SFClient,
) {
    suspend fun sendSporsmal(
        request: SfMeldingRequest,
        ident: String,
        token: String,
    ): Response {
        val nyHenvendelse = sfClient.sendSporsmal(request, ident, token)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun sendInfomelding(
        request: SfMeldingRequest,
        ident: String,
        token: String,
    ): Response {
        val nyHenvendelse = sfClient.sendInfomelding(request, ident, token)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun journalforMelding(
        request: JournalforRequest,
        ident: String,
        token: String,
    ) {
        sfClient.journalforMelding(request, ident, token)
    }

    suspend fun lukkTraad(
        kjedeId: String,
        token: String,
    ) {
        sfClient.lukkTraad(kjedeId, token)
    }
}
