package no.nav.mock

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.*
import no.nav.Consumers
import no.nav.api.digdir.DigdirClient
import no.nav.api.digdir.DigdirClient.*
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.api.skrivestotte.SkrivestotteClient
import no.nav.api.skrivestotte.SkrivestotteClient.*
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.types.identer.NavIdent
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.utils.now
import java.util.*

object MockConsumers : Consumers {
    override val oppfolgingClient = oppfolgingClientMock
    override val tps: PersonV3 = personV3Mock
    override val nom: NomClient = nomClientMock
    override val skrivestotteClient = skrivestotteClientMock
    override val digdirClient = digdirClientMock
}

private val oppfolgingClientMock = mockOf<OppfolgingClient> { client ->
    coEvery { client.hentOppfolgingStatus(any()) } returns OppfolgingClient.Status(
        underOppfolging = true
    )
    coEvery { client.hentOppfolgingVeileder(any()) } returns OppfolgingClient.VeilederId(
        veilederId = "Z123456"
    )
}

private val personV3Mock = mockOf<PersonV3> { tps ->
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

private val skrivestotteClientMock = mockOf<SkrivestotteClient> { client ->
    val hardkodetUUID = UUID.fromString("0a4df913-3651-4667-aac7-ea9a86f1d916")
    val tekst = Tekst(
        id = hardkodetUUID,
        overskrift = "TestTekst",
        tags = emptyList(),
        innhold = Innhold(
            nb_NO = "Dette er en tekst",
            nn_NO = "Dette er ein tekst"
        ),
        vekttall = 0
    )
    val tekster = mapOf(
        hardkodetUUID to tekst,
        UUID.randomUUID() to tekst.copy(
            id = UUID.randomUUID(),
            innhold = Innhold(
                nb_NO = "Dette er ogs?? en tekst",
                en_US = "This is also a text"
            )
        )
    )

    coEvery { client.hentTekster() } returns tekster
}

private val digdirClientMock = mockOf<DigdirClient> { client ->
    val krrData = KrrData(
        personident = "12345678910",
        aktiv = true,
        kanVarsles = true,
        reservert = false,
        epostadresse = "test@nav.no",
        epostadresseOppdatert = LocalDateTime.now(),
        epostadresseVerifisert = LocalDateTime.now(),
    )
    coEvery { client.hentKrrData(any()) } returns krrData
}

inline fun <reified T : Any> mockOf(impl: (T) -> Unit): T {
    return mockk<T>().apply(impl)
}
