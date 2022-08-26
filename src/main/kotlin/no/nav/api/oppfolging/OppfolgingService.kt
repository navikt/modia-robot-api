package no.nav.api.oppfolging

import kotlinx.serialization.Serializable
import no.nav.common.client.nom.NomClient
import no.nav.common.types.identer.NavIdent
import no.nav.utils.externalServiceCall

class OppfolgingService(
    private val oppfolgingClient: OppfolgingClient,
    private val nom: NomClient
) {
    @Serializable
    class Oppfolging(
        val underOppfolging: Boolean,
        val veileder: Veileder?,
    )

    @Serializable
    class Veileder(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    )

    suspend fun hentOppfolging(fnr: String): Oppfolging = externalServiceCall {
        val status = oppfolgingClient.hentOppfolgingStatus(fnr)
        when (status.erUnderOppfolging) {
            null, false -> Oppfolging(underOppfolging = false, veileder = null)
            true -> Oppfolging(underOppfolging = true, veileder = hentVeileder(fnr))
        }
    }

    suspend fun hentVeileder(fnr: String): Veileder? = externalServiceCall {
        val veileder = oppfolgingClient.hentOppfolgingVeileder(fnr)
        veileder?.veilederIdent
            ?.let { nom.finnNavn(NavIdent(it)) }
            ?.let {
                Veileder(
                    ident = it.navIdent.get(),
                    fornavn = it.fornavn,
                    etternavn = it.etternavn
                )
            }
    }
}
