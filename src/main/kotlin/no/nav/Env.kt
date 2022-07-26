package no.nav

import no.nav.utils.getRequiredProperty


interface Env {
    companion object {
        operator fun invoke(): Env = EnvImpl()
    }
    val soapStsUrl: String
    val jwksUrl: String
    val oppfolgingUrl: String
    val tpsPersonV3Url: String
    val nomUrl: String
    val identAllowList: List<String>
}

class EnvImpl : Env {
    override val soapStsUrl: String = getRequiredProperty("SECURITYTOKENSERVICE_URL")
    override val jwksUrl: String = getRequiredProperty("ISSO_JWKS_URL")
    override val oppfolgingUrl: String = getRequiredProperty("OPPFOLGING_URL")
    override val tpsPersonV3Url: String = getRequiredProperty("TPS_PERSONV3_URL")
    override val nomUrl: String = getRequiredProperty("NOM_URL")
    override val identAllowList: List<String> = getRequiredProperty("IDENT_ALLOW_LIST").split(",")

}
