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
    data class MeldingRequest(
        val tekst: String,
        val tema: String,
        val enhet: String
    )

    @Serializable
    data class Response(
        val kjedeId: String,
    )

    @Serializable
    data class SfMeldingRequest(
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

    suspend fun sendSporsmal(fnr:String, request: MeldingRequest): Response {
        val sfMeldingRequest = lagSfMeldingRequest(fnr, request)
        val sak = safService.hentBrukersSaker(fnr).firstOrNull { it.tema?.name == request.tema }
        val nyHenvendelse = sfService.sendSporsmal(sfMeldingRequest)
        val journalforRequest = JournalforRequest(
            journalforendeEnhet = request.enhet,
            fagsakId = sak?.fagsakId,
            fagsaksystem = if (sak?.fagsakId != null) sak.fagsaksystem else null,
            temakode = request.tema,
            kjedeId = nyHenvendelse.kjedeId
        )
        sfService.journalforMelding(journalforRequest)
        return Response(nyHenvendelse.kjedeId)
    }

    suspend fun sendInfomelding(fnr:String, request: MeldingRequest): Response {
        val sfMeldingRequest = lagSfMeldingRequest(fnr, request)
        val sak = safService.hentBrukersSaker(fnr).firstOrNull { it.tema?.name == request.tema }
        val nyHenvendelse = sfService.sendInfomelding(sfMeldingRequest)
        val journalforRequest = JournalforRequest(
            journalforendeEnhet = request.enhet,
            fagsakId = sak?.fagsakId,
            fagsaksystem = if (sak?.fagsakId != null) sak.fagsaksystem else null,
            temakode = request.tema,
            kjedeId = nyHenvendelse.kjedeId
        )
        sfService.lukkTraad(nyHenvendelse.kjedeId)
        sfService.journalforMelding(journalforRequest)
        return Response(nyHenvendelse.kjedeId)
    }
    
    private suspend fun lagSfMeldingRequest(fnr: String, request: MeldingRequest) = SfMeldingRequest(
        aktorId = pdlService.hentAktorid(fnr),
        temagruppe = hentTemagruppeForTema(request.tema),
        enhet = request.enhet,
        fritekst = parseFritekst(fnr, request.tekst),
        tema = request.tema,
        tildelMeg = false
    )
    
    suspend fun parseFritekst(fnr: String, tekst: String): String {
        val navn = pdlService.hentNavn(fnr)
        return tekst
            .replace("[bruker.fornavn]", navn.fornavn)
            .replace("[bruker.etternavn]", navn.etternavn)
            .replace("[bruker.navn]", navn.fulltNavn)
            .replace("[bruker.fnr]", fnr)
    }
}