package no.nav.api.dialog.saf.queries

import kotlinx.serialization.Serializable
import no.nav.utils.*

@Serializable
data class HentBrukerssaker(override val variables: Variables) :
    GraphQLRequest<HentBrukerssaker.Variables, HentBrukerssaker.Result> {
    override val query: String = GraphQLClient.readQuery("saf/queries", "hentBrukerssaker")

    @Serializable
    data class Variables(val brukerId: BrukerIdInput) : GraphQLVariables

    @Serializable
    data class BrukerIdInput(val id: String, val type: BrukerIdType)

    @Serializable
    enum class BrukerIdType {
        AKTOERID, FNR, ORGNR;
    }

    @Serializable
    data class Result(val saker: List<Sak?>) : GraphQLResult

    @Serializable
    data class Sak(
        val fagsakId: String?,
        val fagsaksystem: String?,
        val sakstype: Sakstype?,
        val tema: Tema?
    )

    @Serializable(with = SakstypeEnumSerializer::class)
    enum class Sakstype {
        GENERELL_SAK, FAGSAK, __UNKNOWN_VALUE;
    }

    @Serializable(with = TemaEnumSerializer::class)
    enum class Tema {
        AAP, AAR, AGR, BAR, BID,
        BIL, DAG, ENF, ERS, EYB,
        EYO, FAR, FEI, FOR, FOS,
        FRI, FUL, GEN, GRA, GRU,
        HEL, HJE, IAR, IND, KON,
        KTR, MED, MOB, OMS, OPA,
        OPP, PEN, PER, REH, REK,
        RPO, RVE, SAA, SAK, SAP,
        SER, SIK, STO, SUP, SYK,
        SYM, TIL, TRK, TRY, TSO,
        TSR, UFM, UFO, UKJ, VEN,
        YRA, YRK, __UNKNOWN_VALUE;
    }

    object SakstypeEnumSerializer : EnumSerializer<Sakstype>(Sakstype::class, Sakstype.__UNKNOWN_VALUE)
    object TemaEnumSerializer : EnumSerializer<Tema>(Tema::class, Tema.__UNKNOWN_VALUE)
}