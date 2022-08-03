package no.nav.mock

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.*
import no.nav.Consumers
import no.nav.api.dialog.saf.SafClient
import no.nav.api.dialog.saf.queries.HentBrukerssaker
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.api.pdl.PdlClient
import no.nav.api.pdl.queries.HentPersonalia
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
import no.nav.utils.GraphQLResponse
import no.nav.utils.minus
import no.nav.utils.now
import java.util.*
import kotlin.time.Duration.Companion.days

object MockConsumers : Consumers {
    override val oppfolgingClient = oppfolgingClientMock
    override val tps: PersonV3 = personV3Mock
    override val nom: NomClient = nomClientMock
    override val skrivestotteClient = skrivestotteClientMock
    override val pdlClient = pdlClientMock
    override val safClient = safClientMock
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
                nb_NO = "Dette er også en tekst",
                en_US = "This is also a text"
            )
        )
    )

    coEvery { client.hentTekster() } returns tekster
}

private val pdlClientMock = mockOf<PdlClient> {client ->
    coEvery { client.hentPersonalia(any()) } returns GraphQLResponse(
        data = HentPersonalia.Result(
            hentPerson = HentPersonalia.Person(
                foedsel = listOf(
                    HentPersonalia.Foedsel(
                        foedselsdato = LocalDate.now().minus(10, DateTimeUnit.YEAR)
                    )
                ),
                oppholdsadresse = listOf(
                    HentPersonalia.OppholdsAdresse(
                        gyldigFraOgMed = LocalDateTime.now().minus(2, DateTimeUnit.HOUR),
                        coAdressenavn = "c/o ignore",
                    ),
                    HentPersonalia.OppholdsAdresse(
                        gyldigFraOgMed = LocalDateTime.now().minus(1, DateTimeUnit.HOUR),
                        coAdressenavn = "c/o hansen",
                    )
                )
            )
        )
    )
}

private val safClientMock = mockOf<SafClient> { client ->
    coEvery { client.hentBrukersSaker(any()) } returns GraphQLResponse(
        data = HentBrukerssaker.Result(
            saker = listOf(
                HentBrukerssaker.Sak(
                    fagsakId = null,
                    sakstype = HentBrukerssaker.Sakstype.GENERELL_SAK,
                    tema = HentBrukerssaker.Tema.DAG
                ),
                HentBrukerssaker.Sak(
                    fagsakId = "abba1231",
                    sakstype = HentBrukerssaker.Sakstype.FAGSAK,
                    tema = HentBrukerssaker.Tema.DAG
                )
            )
        )
    )
}

inline fun <reified T : Any> mockOf(impl: (T) -> Unit): T {
    return mockk<T>().apply(impl)
}
