package no.nav.api.syfo

import kotlinx.serialization.Serializable
import no.nav.common.client.nom.NomClient
import no.nav.common.types.identer.NavIdent
import no.nav.utils.externalServiceCall

class SyfoService(
    private val syfoClient: SyfoClient,
    private val nom: NomClient,
) {
    @Serializable
    class Veileder(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    )

    suspend fun hentVeileder(
        fnr: String,
        token: String,
    ): Veileder? =
        externalServiceCall {
            val veileder = syfoClient.hentSyfoVeileder(fnr, token)
            veileder
                ?.tildeltVeilederIdent
                ?.let { nom.finnNavn(NavIdent(it)) }
                ?.let {
                    Veileder(
                        ident = it.navIdent.get(),
                        fornavn = it.fornavn,
                        etternavn = it.etternavn,
                    )
                }
        }
}
