package no.nav.api.dialog

import kotlinx.serialization.Serializable
import no.nav.api.dialog.saf.SafService
import no.nav.api.dialog.sf.SFService
import no.nav.api.pdl.PdlService
import no.nav.modiapersonoversikt.commondomain.TemagruppeTemaMapping.hentTemagruppeForTema

class DialogService(
    private val safService: SafService,
    private val sfService: SFService,
    private val pdlService: PdlService
) {
    @Serializable
    data class SendInfomeldingRequest(
        val tekst: String,
        val tema: String,
        val enhet: String
    )

    @Serializable
    data class SendSporsmalRequest(
        val tekst: String,
        val tema: String,
        val enhet: String
    )

    @Serializable
    data class Response(
        val kjedeId: String,
    )

    @Serializable
    data class MeldingRequest(
        val aktorId: String,
        val temagruppe: String,
        val enhet: String,
        val fritekst: String,
        val tema: String? = null,
        val tildelMeg: Boolean? = null
    )

    @Serializable
    data class JournalforRequest(
        val journalforendeEnhet: String,
        val fagsakId: String? = null,
        val fagsaksystem: String? = null,
        val temakode: String,
        val kjedeId: String
    )

    suspend fun sendSporsmal(fnr:String, request: SendSporsmalRequest): Response {
        val meldingRequest = MeldingRequest(
            aktorId = pdlService.hentAktorid(fnr),
            temagruppe = hentTemagruppeForTema(request.tema),
            enhet = request.enhet,
            fritekst = request.tekst,
            tema = request.tema,
            tildelMeg = false
        )
        val sak = safService.hentBrukersSaker(fnr).firstOrNull { it.tema?.name == request.tema }
        val nyHenvendelse = sfService.sendSporsmal(meldingRequest)
        val journalforRequest = JournalforRequest(
            journalforendeEnhet = request.enhet,
            fagsakId = sak?.fagsakId,
            fagsaksystem = sak?.fagsaksystem,
            temakode = request.tema,
            kjedeId = nyHenvendelse.kjedeId
        )
        sfService.journalforMelding(journalforRequest)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun sendInfomelding(fnr:String, request: SendInfomeldingRequest): Response {
        val meldingRequest = MeldingRequest(
            aktorId = pdlService.hentAktorid(fnr),
            temagruppe = hentTemagruppeForTema(request.tema),
            enhet = request.enhet,
            fritekst = request.tekst,
            tema = request.tema,
            tildelMeg = false
        )
        val sak = safService.hentBrukersSaker(fnr).firstOrNull { it.tema?.name == request.tema }
        val nyHenvendelse = sfService.sendInfomelding(meldingRequest)
        val journalforRequest = JournalforRequest(
            journalforendeEnhet = request.enhet,
            fagsakId = sak?.fagsakId,
            fagsaksystem = sak?.fagsaksystem,
            temakode = request.tema,
            kjedeId = nyHenvendelse.kjedeId
        )
        sfService.lukkTraad(nyHenvendelse.kjedeId)
        sfService.journalforMelding(journalforRequest)
        return Response(nyHenvendelse.kjedeId)
    }
}