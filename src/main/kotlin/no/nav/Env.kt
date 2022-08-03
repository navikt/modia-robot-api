package no.nav

import no.nav.personoversikt.utils.EnvUtils.getRequiredConfig
import no.nav.utils.DownstreamApi
import no.nav.utils.parse

interface Env {
    companion object {
        operator fun invoke(): Env = EnvImpl()
    }
    val soapStsUrl: String
    val jwksUrl: String
    val oppfolgingUrl: String
    val oppfolgingScope: DownstreamApi
    val tpsPersonV3Url: String
    val nomUrl: String
    val nomScope: DownstreamApi
    val identAllowList: List<String>
    val skrivestotteUrl: String
    val digdirUrl: String
    val digdirScope: DownstreamApi
    val utbetalingerUrl: String
}

class EnvImpl : Env {
    override val soapStsUrl: String = getRequiredConfig("SECURITYTOKENSERVICE_URL")
    override val jwksUrl: String = getRequiredConfig("ISSO_JWKS_URL")
    override val oppfolgingUrl: String = getRequiredConfig("OPPFOLGING_URL")
    override val oppfolgingScope: DownstreamApi = getRequiredConfig("OPPFOLGING_SCOPE").toDownstreamApi()
    override val tpsPersonV3Url: String = getRequiredConfig("TPS_PERSONV3_URL")
    override val nomUrl: String = getRequiredConfig("NOM_URL")
    override val nomScope: DownstreamApi = getRequiredConfig("NOM_SCOPE").toDownstreamApi()
    override val identAllowList: List<String> = getRequiredConfig("IDENT_ALLOW_LIST").split(",")
    override val skrivestotteUrl: String = getRequiredConfig("SKRIVESTOTTE_URL")
    override val digdirUrl: String = getRequiredConfig("DIGDIR_KRR_URL")
    override val digdirScope: DownstreamApi = getRequiredConfig("DIGDIR_KRR_SCOPE").toDownstreamApi()
    override val utbetalingerUrl: String = getRequiredConfig("UTBETALING_V1_URL")
}

private fun String.toDownstreamApi() = DownstreamApi.parse(this)