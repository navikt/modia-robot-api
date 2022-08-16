import kotlinx.serialization.Serializable
import no.nav.utils.GraphQLClient
import no.nav.utils.GraphQLRequest
import no.nav.utils.GraphQLResult
import no.nav.utils.GraphQLVariables

@Serializable
data class HentAktorid(override val variables: Variables)
    : GraphQLRequest<HentAktorid.Variables, HentAktorid.Result> {
    override val query: String = GraphQLClient.readQuery("pdl", "hentAktorid")

    @Serializable
    data class Variables(val ident: String, val grupper: List<IdentGruppe> = listOf(IdentGruppe.AKTORID)) : GraphQLVariables

    @Serializable
    enum class IdentGruppe {
        AKTORID
    }

    @Serializable
    data class IdentInfo(val ident: String)

    @Serializable
    data class IdentListe(val identer: List<IdentInfo>)

    @Serializable
    data class Result(val hentIdenter: IdentListe?) : GraphQLResult
}