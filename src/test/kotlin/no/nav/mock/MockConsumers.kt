package no.nav.mock

import com.expediagroup.graphql.client.serialization.types.KotlinxGraphQLResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.*
import no.nav.Consumers
import no.nav.api.digdir.DigdirClient
import no.nav.api.digdir.DigdirClient.*
import no.nav.api.dialog.saf.SafClient
import no.nav.api.dialog.sf.SFClient
import no.nav.api.generated.pdl.HentAktorid
import no.nav.api.generated.pdl.HentNavn
import no.nav.api.generated.pdl.hentaktorid.*
import no.nav.api.generated.pdl.hentpersonalia.*
import no.nav.api.generated.pdl.HentPersonalia
import no.nav.api.generated.pdl.hentnavn.Navn
import no.nav.api.generated.pdl.hentnavn.Person
import no.nav.api.generated.saf.HentBrukerssaker
import no.nav.api.generated.saf.enums.Sakstype
import no.nav.api.generated.saf.enums.Tema
import no.nav.api.generated.saf.hentbrukerssaker.Sak
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.api.pdl.PdlClient
import no.nav.api.skrivestotte.SkrivestotteClient
import no.nav.api.skrivestotte.SkrivestotteClient.*
import no.nav.api.utbetalinger.UtbetalingerClient
import no.nav.api.utbetalinger.utbetalinger
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.types.identer.NavIdent
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.utils.minus
import no.nav.utils.now
import java.util.*

object MockConsumers : Consumers {
    override val tokenclient = tokenClientMock
    override val oppfolgingClient = oppfolgingClientMock
    override val tps: PersonV3 = personV3Mock
    override val nom: NomClient = nomClientMock
    override val skrivestotteClient = skrivestotteClientMock
    override val digdirClient = digdirClientMock
    override val utbetalingerClient = utbetalingerMock
    override val pdlClient = pdlClientMock
    override val safClient = safClientMock
    override val sfClient = sfClientMock
}

private val tokenClientMock = mockOf<MachineToMachineTokenClient> { client ->
    every { client.createMachineToMachineToken(any()) } returns UUID.randomUUID().toString()
}

private val oppfolgingClientMock = mockOf<OppfolgingClient> { client ->
    coEvery { client.hentOppfolgingStatus(any()) } returns OppfolgingClient.Status(
        erUnderOppfolging = true
    )
    coEvery { client.hentOppfolgingVeileder(any()) } returns OppfolgingClient.VeilederIdent(
        veilederIdent = "Z123456"
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
        tags = listOf("nøs", "kontonummer", "retur"),
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
                nb_NO = "Dette er også en tekst",
                en_US = "This is also a text"
            ),
            tags = emptyList()
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
        epostadresseOppdatert = Instant.parse("2019-03-06T15:29:41Z"),
        epostadresseVerifisert = Clock.System.now(),
        mobiltelefonnummer = "90909090",
        mobiltelefonnummerOppdatert = Clock.System.now(),
        mobiltelefonnummerVerifisert = Clock.System.now()
    )
    coEvery { client.hentKrrData(any()) } returns krrData
}

private val utbetalingerMock = mockOf<UtbetalingerClient> { client ->
    coEvery { client.hentUtbetalinger(any(), any(), any()) } returns utbetalinger
}

private val pdlClientMock = mockOf<PdlClient> { client ->
    coEvery { client.hentPersonalia(any()) } returns KotlinxGraphQLResponse(
        data = HentPersonalia.Result(
            hentPerson = Person(
                foedsel = listOf(
                    Foedsel(
                        foedselsdato = LocalDate.now().minus(10, DateTimeUnit.YEAR)
                    )
                ),
                oppholdsadresse = listOf(
                    Oppholdsadresse(
                        gyldigFraOgMed = LocalDateTime.now().minus(2, DateTimeUnit.HOUR),
                        coAdressenavn = "c/o ignore",
                    ),
                    Oppholdsadresse(
                        gyldigFraOgMed = LocalDateTime.now().minus(1, DateTimeUnit.HOUR),
                        coAdressenavn = "c/o hansen",
                    )
                ),
                kontaktadresse = emptyList(),
                bostedsadresse = emptyList()
            )
        )
    )
    coEvery { client.hentAktorid(any()) } returns KotlinxGraphQLResponse(
        data = HentAktorid.Result(
            hentIdenter = Identliste(
                identer = listOf(
                    IdentInformasjon(
                        ident = "10108000398"
                    )
                )
            )
        )
    )
    
    coEvery { client.hentNavn(any()) } returns KotlinxGraphQLResponse(
        data = HentNavn.Result(
            hentPerson = Person(
                navn = listOf(
                    Navn(
                        fornavn = "Aremark",
                        mellomnavn = null,
                        etternavn = "Testfamilien"
                    )
                )
            )
        )
    )
}

private val safClientMock = mockOf<SafClient> { client ->
    coEvery { client.hentBrukersSaker(any()) } returns KotlinxGraphQLResponse(
        data = HentBrukerssaker.Result(
            saker = listOf(
                Sak(
                    fagsakId = null,
                    fagsaksystem = null,
                    sakstype = Sakstype.GENERELL_SAK,
                    tema = Tema.DAG
                ),
                Sak(
                    fagsakId = "abba1231",
                    fagsaksystem = "AO01",
                    sakstype = Sakstype.FAGSAK,
                    tema = Tema.DAG
                )
            )
        )
    )
}

private val sfClientMock = mockOf<SFClient> { client ->
    coEvery { client.sendSporsmal(any(), any()) } returns SFClient.HenvendelseDTO(kjedeId = "1234")
    coEvery { client.sendInfomelding(any(), any()) } returns SFClient.HenvendelseDTO(kjedeId = "5678")
    coEvery { client.journalforMelding(any(), any()) } returns Unit
    coEvery { client.lukkTraad(any()) } returns Unit
}

inline fun <reified T : Any> mockOf(impl: (T) -> Unit): T {
    return mockk<T>().apply(impl)
}
