package no.nav.api.dialog.sf

import no.nav.api.dialog.DialogService.*

class SFService(
    private val sfClient: SFClient
    ) {
    suspend fun sendSporsmal(request: MeldingRequest): Response {
        val nyHenvendelse = sfClient.sendSporsmal(request)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun sendInfomelding(request: MeldingRequest): Response {
        val nyHenvendelse = sfClient.sendInfomelding(request)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun journalforMelding(request: JournalforRequest) {
        sfClient.journalforMelding(request)
    }

    suspend fun lukkTraad(kjedeId: String) {
        sfClient.lukkTraad(kjedeId)
    }
}