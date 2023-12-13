package no.nav.api.tps

import kotlinx.serialization.Serializable
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.utils.externalServiceCall

class TpsService(
    private val tps: PersonV3,
) {
    @Serializable
    data class Kontonummer(
        val kontonummer: String?,
    )

    private val fantIkkeKontonummer = Kontonummer(null)

    suspend fun hentKontonummer(fnr: String): Kontonummer =
        externalServiceCall {
            val request =
                HentPersonRequest()
                    .withAktoer(PersonIdent().withIdent(NorskIdent().withIdent(fnr)))
                    .withInformasjonsbehov(
                        Informasjonsbehov.BANKKONTO,
                        Informasjonsbehov.SPORINGSINFORMASJON,
                    )

            val response = tps.hentPerson(request)
            val person = response.person

            if (person !is Bruker) {
                fantIkkeKontonummer
            } else {
                when (val bankkonto = person.bankkonto) {
                    is BankkontoNorge -> Kontonummer(bankkonto.bankkonto.bankkontonummer)
                    is BankkontoUtland -> Kontonummer(bankkonto.bankkontoUtland.bankkontonummer)
                    else -> fantIkkeKontonummer
                }
            }
        }
}
