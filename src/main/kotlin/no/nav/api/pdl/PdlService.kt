package no.nav.api.pdl

import kotlinx.datetime.*
import no.nav.api.generated.pdl.hentpersonalia.*
import no.nav.api.kodeverk.KodeverkNavn
import no.nav.api.kodeverk.KodeverkService
import no.nav.utils.TjenestekallLogger
import no.nav.utils.now
import no.nav.api.generated.pdl.hentpersonalia.Navn as PdlNavn

class PdlService(
    private val client: PdlClient,
    private val kodeverk: KodeverkService,
) {
    data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
    ) {
        val fulltNavn = listOfNotNull(fornavn, mellomnavn, etternavn).joinToString(" ")

        companion object {
            val UKJENT = Navn("", "", "")
        }
    }

    suspend fun hentPersonalia(
        fnr: String,
        token: String,
    ): PdlPersonalia {
        val person = client.hentPersonalia(fnr, token).data?.hentPerson
        return PdlPersonalia(
            navn = person?.let(::hentNavnPersonalia),
            alder = person?.let(::hentAlder),
            bostedsAdresse = person?.let(::hentBostedsAdresse),
            kontaktAdresse = person?.let(::hentKontaktAdresse),
            oppholdsAdresse = person?.let(::hentOppholdsAdresse),
        )
    }

    suspend fun hentAktorid(
        fnr: String,
        token: String,
    ): String =
        requireNotNull(
            client
                .hentAktorid(fnr, token)
                .data
                ?.hentIdenter
                ?.identer
                ?.firstOrNull()
                ?.ident,
        )

    suspend fun hentAktoridNullable(
        fnr: String,
        token: String,
    ): String? =
        client
            .hentAktorid(fnr, token)
            .data
            ?.hentIdenter
            ?.identer
            ?.firstOrNull()
            ?.ident

    suspend fun hentNavn(
        fnr: String,
        token: String,
    ): Navn {
        val navn =
            client
                .hentNavn(fnr, token)
                .data
                ?.hentPerson
                ?.navn
                ?.firstOrNull() ?: return Navn.UKJENT
        return Navn(
            fornavn = navn.fornavn,
            mellomnavn = navn.mellomnavn,
            etternavn = navn.etternavn,
        )
    }

    fun hentNavnPersonalia(person: Person?): PdlNavn {
        val navn =
            person?.navn?.firstOrNull() ?: PdlNavn("", "", "")
        return PdlNavn(
            fornavn = navn.fornavn,
            mellomnavn = navn.mellomnavn,
            etternavn = navn.etternavn,
        )
    }

    private fun hentAlder(person: Person?): Int? =
        person
            ?.foedselsdato
            ?.firstOrNull()
            ?.foedselsdato
            ?.periodUntil(LocalDate.now())
            ?.years

    private fun hentBostedsAdresse(person: Person?): PdlAdresse? =
        person
            ?.bostedsadresse
            ?.sortedByDescending { it.gyldigFraOgMed }
            ?.firstNotNullOfOrNull { adresse ->
                when {
                    adresse.vegadresse != null -> lagAdresseFraVegadresse(adresse.vegadresse)
                    adresse.matrikkeladresse != null -> lagAdresseFraMatrikkeladresse(adresse.matrikkeladresse)
                    adresse.utenlandskAdresse != null -> lagAdresseFraUtenlandskadresse(adresse.utenlandskAdresse)
                    adresse.ukjentBosted != null -> lagAdresseFraUkjentbosted(adresse.ukjentBosted)
                    else -> {
                        TjenestekallLogger.error(
                            "PdlService",
                            mapOf(
                                "feil" to "fant ukjent adresse format for bostedsadresse",
                                "adresse" to adresse,
                            ),
                        )
                        null
                    }
                }
            }

    private fun hentKontaktAdresse(person: Person?): PdlAdresse? =
        person
            ?.kontaktadresse
            ?.sortedByDescending { it.gyldigFraOgMed }
            ?.firstNotNullOfOrNull { adresse ->
                when {
                    adresse.coAdressenavn.isNullOrBlank().not() && adresse.vegadresse != null ->
                        lagAdresseFraCoVegadresse(
                            adresse.coAdressenavn!!,
                            lagAdresseFraVegadresse(adresse.vegadresse),
                        )

                    adresse.coAdressenavn.isNullOrBlank().not() -> lagAdresseFraCoadresse(adresse.coAdressenavn!!)
                    adresse.vegadresse != null -> lagAdresseFraVegadresse(adresse.vegadresse)
                    adresse.postboksadresse != null -> lagAdresseFraPostboksadresse(adresse.postboksadresse)
                    adresse.postadresseIFrittFormat != null -> lagAdresseFraFrittformat(adresse.postadresseIFrittFormat)
                    adresse.utenlandskAdresse != null -> lagAdresseFraUtenlandskadresse(adresse.utenlandskAdresse)
                    adresse.utenlandskAdresseIFrittFormat != null ->
                        lagAdresseFraUtenlandskadresseFrittFormat(
                            adresse.utenlandskAdresseIFrittFormat,
                        )

                    else -> {
                        TjenestekallLogger.error(
                            "PdlService",
                            mapOf(
                                "feil" to "fant ukjent adresse format for kontaktadresse",
                                "adresse" to adresse,
                            ),
                        )
                        null
                    }
                }
            }

    private fun hentOppholdsAdresse(person: Person?): PdlAdresse? =
        person
            ?.oppholdsadresse
            ?.sortedByDescending { it.gyldigFraOgMed }
            ?.firstNotNullOfOrNull { adresse ->
                when {
                    adresse.coAdressenavn.isNullOrBlank().not() && adresse.vegadresse != null ->
                        lagAdresseFraCoVegadresse(
                            adresse.coAdressenavn!!,
                            lagAdresseFraVegadresse(adresse.vegadresse),
                        )

                    adresse.coAdressenavn.isNullOrBlank().not() -> lagAdresseFraCoadresse(adresse.coAdressenavn!!)
                    adresse.vegadresse != null -> lagAdresseFraVegadresse(adresse.vegadresse)
                    adresse.matrikkeladresse != null -> lagAdresseFraMatrikkeladresse(adresse.matrikkeladresse)
                    adresse.utenlandskAdresse != null -> lagAdresseFraUtenlandskadresse(adresse.utenlandskAdresse)
                    else -> {
                        TjenestekallLogger.error(
                            "PdlService",
                            mapOf(
                                "feil" to "fant ukjent adresse format for bostedsadresse",
                                "adresse" to adresse,
                            ),
                        )
                        null
                    }
                }
            }

    private fun lagAdresseFraVegadresse(adresse: Vegadresse) =
        PdlAdresse(
            linje1 = listOf(adresse.adressenavn, adresse.husnummer, adresse.husbokstav, adresse.bruksenhetsnummer),
            linje2 =
                listOf(
                    adresse.postnummer,
                    adresse.postnummer?.let { kodeverk.hentKodeBeskrivelse(KodeverkNavn.POSTNUMMER, it, it) },
                    adresse.bydelsnummer?.let { "Bydel: ${kodeverk.hentKodeBeskrivelse(KodeverkNavn.KOMMUNE, it, it) }" },
                    adresse.kommunenummer?.let { "Kommune: ${kodeverk.hentKodeBeskrivelse(KodeverkNavn.KOMMUNE, it, it) }" },
                ),
        )

    private fun lagAdresseFraMatrikkeladresse(adresse: Matrikkeladresse) =
        PdlAdresse(
            linje1 = listOf(adresse.bruksenhetsnummer, adresse.tilleggsnavn),
            linje2 =
                listOf(
                    adresse.postnummer,
                    adresse.postnummer?.let { kodeverk.hentKodeBeskrivelse(KodeverkNavn.POSTNUMMER, it, it) },
                    adresse.kommunenummer?.let { "Kommune: ${kodeverk.hentKodeBeskrivelse(KodeverkNavn.KOMMUNE, it, it) }" },
                ),
        )

    private fun lagAdresseFraUtenlandskadresse(adresse: UtenlandskAdresse) =
        PdlAdresse(
            linje1 = listOf(adresse.postboksNummerNavn, adresse.adressenavnNummer, adresse.bygningEtasjeLeilighet),
            linje2 = listOf(adresse.postkode, adresse.bySted, adresse.regionDistriktOmraade),
            linje3 =
                listOf(
                    adresse.landkode,
                    adresse.landkode.let {
                        kodeverk.hentKodeBeskrivelse(KodeverkNavn.LAND, it, it)
                    },
                ),
        )

    private fun lagAdresseFraUtenlandskadresseFrittFormat(adresse: UtenlandskAdresseIFrittFormat) =
        PdlAdresse(
            linje1 = listOf(adresse.adresselinje1),
            linje2 = listOf(adresse.adresselinje2),
            linje3 =
                listOf(
                    adresse.adresselinje3,
                    adresse.postkode,
                    adresse.byEllerStedsnavn,
                    adresse.landkode,
                    adresse.landkode.let {
                        kodeverk.hentKodeBeskrivelse(KodeverkNavn.LAND, it, it)
                    },
                ),
        )

    private fun lagAdresseFraUkjentbosted(adresse: UkjentBosted) =
        PdlAdresse(
            linje1 = listOf(adresse.bostedskommune ?: "Ukjent kommune"),
        )

    private fun lagAdresseFraCoVegadresse(
        coAdresse: String,
        adresse: PdlAdresse,
    ) = PdlAdresse(
        linje1 = listOf(coAdresse),
        linje2 = listOf(adresse.linje1),
        linje3 = listOf(adresse.linje2),
    )

    private fun lagAdresseFraCoadresse(coAdresse: String) =
        PdlAdresse(
            linje1 = listOf(coAdresse),
        )

    private fun lagAdresseFraPostboksadresse(adresse: Postboksadresse) =
        PdlAdresse(
            linje1 = listOf(adresse.postbokseier),
            linje2 = listOf("Postboks", adresse.postboks),
            linje3 = listOf(adresse.postnummer, adresse.postnummer?.let { kodeverk.hentKodeBeskrivelse(KodeverkNavn.POSTNUMMER, it, it) }),
        )

    private fun lagAdresseFraFrittformat(adresse: PostadresseIFrittFormat) =
        PdlAdresse(
            linje1 = listOf(adresse.adresselinje1),
            linje2 = listOf(adresse.adresselinje2),
            linje3 =
                listOf(
                    adresse.adresselinje3,
                    adresse.postnummer,
                    adresse.postnummer?.let {
                        kodeverk.hentKodeBeskrivelse(KodeverkNavn.POSTNUMMER, it, it)
                    },
                ),
        )
}
