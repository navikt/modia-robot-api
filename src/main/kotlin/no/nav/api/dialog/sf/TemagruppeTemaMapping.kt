package no.nav.modiapersonoversikt.commondomain

import no.nav.api.dialog.sf.Temagruppe

object TemagruppeTemaMapping {
    val TEMA_UTEN_TEMAGRUPPE: Temagruppe = Temagruppe.OVRG
    val TEMA_TEMAGRUPPE_MAPPING: Map<String, String> = mapOf(
        "AAP" to Temagruppe.ARBD.name,
        "DAG" to Temagruppe.ARBD.name,
        "FOS" to Temagruppe.ARBD.name,
        "IND" to Temagruppe.ARBD.name,
        "MOB" to Temagruppe.ARBD.name,
        "OPP" to Temagruppe.ARBD.name,
        "REH" to Temagruppe.ARBD.name,
        "SAK" to Temagruppe.ARBD.name,
        "SAP" to Temagruppe.ARBD.name,
        "SYK" to Temagruppe.ARBD.name,
        "SYM" to Temagruppe.ARBD.name,
        "VEN" to Temagruppe.ARBD.name,
        "YRA" to Temagruppe.ARBD.name,
        "YRK" to Temagruppe.ARBD.name,
        "TSO" to Temagruppe.ARBD.name,
        "TSR" to Temagruppe.ARBD.name,

        "BAR" to Temagruppe.FMLI.name,
        "BID" to Temagruppe.FMLI.name,
        "ENF" to Temagruppe.FMLI.name,
        "FOR" to Temagruppe.FMLI.name,
        "GRA" to Temagruppe.FMLI.name,
        "GRU" to Temagruppe.FMLI.name,
        "KON" to Temagruppe.FMLI.name,
        "OMS" to Temagruppe.FMLI.name,

        "AAR" to Temagruppe.OVRG.name,
        "AGR" to Temagruppe.OVRG.name,
        "ERS" to Temagruppe.OVRG.name,
        "FEI" to Temagruppe.OVRG.name,
        "FUL" to Temagruppe.OVRG.name,
        "GEN" to Temagruppe.OVRG.name,
        "KLA" to Temagruppe.OVRG.name,
        "KNA" to Temagruppe.OVRG.name,
        "KTR" to Temagruppe.OVRG.name,
        "MED" to Temagruppe.OVRG.name,
        "RVE" to Temagruppe.OVRG.name,
        "RPO" to Temagruppe.OVRG.name,
        "SER" to Temagruppe.OVRG.name,
        "SIK" to Temagruppe.OVRG.name,
        "STO" to Temagruppe.OVRG.name,
        "TRK" to Temagruppe.OVRG.name,
        "TRY" to Temagruppe.OVRG.name,
        "UFM" to Temagruppe.OVRG.name,

        "PEN" to Temagruppe.PENS.name,
        "SUP" to Temagruppe.PENS.name,
        "UFO" to Temagruppe.PENS.name,
        "HJE" to Temagruppe.HJLPM.name
    )

    fun hentTemagruppeForTema(tema: String): String {
        return if (TEMA_TEMAGRUPPE_MAPPING[tema] == null) TEMA_UTEN_TEMAGRUPPE.name else TEMA_TEMAGRUPPE_MAPPING[tema]!!
    }
}