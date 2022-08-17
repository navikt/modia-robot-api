package no.nav.api.pdl.queries

import kotlinx.serialization.Serializable
import no.nav.utils.GraphQLClient.Companion.readQuery
import no.nav.utils.GraphQLRequest
import no.nav.utils.GraphQLResult
import no.nav.utils.GraphQLVariables

@Serializable
data class HentNavn(override val variables: Variables)
    : GraphQLRequest<HentNavn.Variables, HentNavn.Result> {
    override val query: String = readQuery("pdl", "hentNavn")

    @Serializable
    data class Variables(val ident: String) : GraphQLVariables

    @Serializable
    data class Result(val hentPerson: Person) : GraphQLResult

    @Serializable
    data class Person(
        val navn: List<Navn> = emptyList(),
    )
    
    @Serializable
    data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
    )
}