package no.nav.api.pdl.queries

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import no.nav.utils.GraphQLClient.Companion.readQuery
import no.nav.utils.GraphQLRequest
import no.nav.utils.GraphQLResult
import no.nav.utils.GraphQLVariables

@Serializable
data class HentPersonalia(override val variables: Variables)
    : GraphQLRequest<HentPersonalia.Variables, HentPersonalia.Result> {
    override val query: String = readQuery("pdl", "hentPersonalia")

    @Serializable
    data class Variables(val ident: String) : GraphQLVariables

    @Serializable
    data class Result(val hentPerson: Person?) : GraphQLResult

    @Serializable
    data class Person(
        val foedsel: List<Foedsel> = emptyList(),
        val oppholdsadresse: List<OppholdsAdresse> = emptyList(),
        val kontaktadresse: List<KontaktAdresse> = emptyList(),
        val bostedsadresse: List<BostedsAdresse> = emptyList(),
    )

    @Serializable
    data class Foedsel(
        val foedselsdato: LocalDate? = null
    )

    @Serializable
    data class OppholdsAdresse(
        val gyldigFraOgMed: LocalDateTime? = null,
        val gyldigTilOgMed: LocalDateTime? = null,
        val oppholdAnnetSted: String? = null,
        val coAdressenavn: String? = null,
        val vegadresse: Vegadresse? = null,
        val matrikkeladresse: Matrikkeladresse? = null,
        val utenlandskAdresse: UtenlandskAdresse? = null,
    )

    @Serializable
    data class KontaktAdresse(
        val gyldigFraOgMed: LocalDateTime? = null,
        val gyldigTilOgMed: LocalDateTime? = null,
        val coAdressenavn: String? = null,
        val postadresseIFrittFormat: PostadresseIFrittFormat? = null,
        val postboksadresse: Postboksadresse? = null,
        val vegadresse: Vegadresse? = null,
        val utenlandskAdresse: UtenlandskAdresse? = null,
        val utenlandskAdresseIFrittFormat: UtenlandskAdresseIFrittFormat? = null,
    )

    @Serializable
    data class PostadresseIFrittFormat(
        val adresselinje1: String? = null,
        val adresselinje2: String? = null,
        val adresselinje3: String? = null,
        val postnummer: String? = null,
    )

    @Serializable
    data class UtenlandskAdresseIFrittFormat(
        val adresselinje1: String? = null,
        val adresselinje2: String? = null,
        val adresselinje3: String? = null,
        val postkode: String? = null,
        val byEllerStedsnavn: String? = null,
        val landkode: String,
    )

    @Serializable
    data class Postboksadresse(
        val postbokseier: String? = null,
        val postboks: String,
        val postnummer: String? = null,
    )

    @Serializable
    data class BostedsAdresse(
        val gyldigFraOgMed: LocalDateTime? = null,
        val gyldigTilOgMed: LocalDateTime? = null,
        val vegadresse: Vegadresse? = null,
        val matrikkeladresse: Matrikkeladresse? = null,
        val utenlandskAdresse: UtenlandskAdresse? = null,
        val ukjentBosted: UkjentBosted? = null,
    )

    @Serializable
    data class UkjentBosted(
        val bostedskommune: String? = null,
    )

    @Serializable
    data class Vegadresse(
        val matrikkelId: Long? = null,
        val husnummer: String? = null,
        val husbokstav: String? = null,
        val bruksenhetsnummer: String? = null,
        val adressenavn: String? = null,
        val kommunenummer: String? = null,
        val bydelsnummer: String? = null,
        val tilleggsnavn: String? = null,
        val postnummer: String? = null,
    )

    @Serializable
    data class Matrikkeladresse(
        val matrikkelId: Long? = null,
        val bruksenhetsnummer: String? = null,
        val tilleggsnavn: String? = null,
        val postnummer: String? = null,
        val kommunenummer: String? = null,
    )

    @Serializable
    data class UtenlandskAdresse(
        val adressenavnNummer: String? = null,
        val bygningEtasjeLeilighet: String? = null,
        val postboksNummerNavn: String? = null,
        val postkode: String? = null,
        val bySted: String? = null,
        val regionDistriktOmraade: String? = null,
        val landkode: String,
    )

    @Serializable
    data class Endring(
        val registrert: LocalDateTime,
        val registrertAv: String,
        val systemkilde: String,
        val kilde: String,
    )
}