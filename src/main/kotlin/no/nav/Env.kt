package no.nav

import no.nav.personoversikt.utils.EnvUtils.getRequiredConfig

interface Env {
    companion object {
        operator fun invoke(): Env = EnvImpl()
    }
    val soapStsUrl: String
    val jwksUrl: String
    val oppfolgingUrl: String
    val tpsPersonV3Url: String
    val nomUrl: String
    val pdlUrl: String
    val safUrl: String
    val identAllowList: List<String>
    val skrivestotteUrl: String
}

class EnvImpl : Env {
    override val soapStsUrl: String = getRequiredConfig("SECURITYTOKENSERVICE_URL")
    override val jwksUrl: String = getRequiredConfig("ISSO_JWKS_URL")
    override val oppfolgingUrl: String = getRequiredConfig("OPPFOLGING_URL")
    override val tpsPersonV3Url: String = getRequiredConfig("TPS_PERSONV3_URL")
    override val nomUrl: String = getRequiredConfig("NOM_URL")
    override val pdlUrl: String = getRequiredConfig("PDL_URL")
    override val safUrl: String = getRequiredConfig("SAF_URL")
    override val identAllowList: List<String> = getRequiredConfig("IDENT_ALLOW_LIST").split(",")
    override val skrivestotteUrl = getRequiredConfig("SKRIVESTOTTE_URL")
}
