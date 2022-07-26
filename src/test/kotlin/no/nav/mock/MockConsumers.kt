package no.nav.mock

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.Consumers
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.types.identer.NavIdent
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse

object MockConsumers : Consumers {
    override val oppfolgingClient = oppfolgingClientMock
    override val tps: PersonV3 = personV3Mock
    override val nom: NomClient = nomClientMock
}

private val oppfolgingClientMock = mockOf<OppfolgingClient> { client ->
    coEvery { client.hentOppfolgingStatus(any()) } returns OppfolgingClient.Status(
        underOppfolging = true
    )
    coEvery { client.hentOppfolgingVeileder(any()) } returns OppfolgingClient.VeilederId(
        veilederId = "Z123456"
    )
}

private val personV3Mock = mockOf<PersonV3> {tps ->
    coEvery { tps.hentPerson(any()) } returns HentPersonResponse().withPerson(
        Bruker().withBankkonto(
            BankkontoNorge().withBankkonto(
                Bankkontonummer().withBankkontonummer("123456789123456")
            )
        )
    )
}

private val nomClientMock = mockOf<NomClient> { client ->
    coEvery { client.finnNavn(any<NavIdent>()) } returns VeilederNavn()
        .setNavIdent(NavIdent("Z999999"))
        .setFornavn("Fornavn")
        .setEtternavn("Etternavn")
        .setVisningsNavn("Fornavn Etternavn")
}


inline fun <reified T : Any> mockOf(impl: (T) -> Unit): T {
    return mockk<T>().apply(impl)
}