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
    val tpsPersonV3Url: String
    val oppfolgingUrl: String
    val oppfolgingScope: DownstreamApi
    val nomUrl: String
    val nomScope: DownstreamApi
    val pdlUrl: String
    val pdlScope: DownstreamApi
    val digdirUrl: String
    val digdirScope: DownstreamApi
    val utbetalingerUrl: String
    val safUrl: String
    val safScope: DownstreamApi
    val skrivestotteUrl: String
    val sfUrl: String
    val sfScope: DownstreamApi
    val identAllowList: List<String>
}

class EnvImpl : Env {
    override val soapStsUrl: String = getRequiredConfig("SECURITYTOKENSERVICE_URL")
    override val jwksUrl: String = getRequiredConfig("ISSO_JWKS_URL")
    override val tpsPersonV3Url: String = getRequiredConfig("TPS_PERSONV3_URL")
    override val oppfolgingUrl: String = getRequiredConfig("OPPFOLGING_URL")
    override val oppfolgingScope: DownstreamApi = getRequiredConfig("OPPFOLGING_SCOPE").toDownstreamApi()
    override val nomUrl: String = getRequiredConfig("NOM_URL")
    override val nomScope: DownstreamApi = getRequiredConfig("NOM_SCOPE").toDownstreamApi()
    override val pdlUrl: String = getRequiredConfig("PDL_URL")
    override val pdlScope: DownstreamApi = getRequiredConfig("PDL_SCOPE").toDownstreamApi()
    override val digdirUrl: String = getRequiredConfig("DIGDIR_KRR_URL")
    override val digdirScope: DownstreamApi = getRequiredConfig("DIGDIR_KRR_SCOPE").toDownstreamApi()
    override val utbetalingerUrl: String = getRequiredConfig("UTBETALING_V1_URL")
    override val safUrl: String = getRequiredConfig("SAF_URL")
    override val safScope: DownstreamApi = getRequiredConfig("SAF_SCOPE").toDownstreamApi()
    override val skrivestotteUrl: String = getRequiredConfig("SKRIVESTOTTE_URL")
    override val sfUrl: String = getRequiredConfig("SF_HENVENDELSE_URL")
    override val sfScope: DownstreamApi = getRequiredConfig("SF_HENVENDELSE_SCOPE").toDownstreamApi()
    override val identAllowList: List<String> = getRequiredConfig("IDENT_ALLOW_LIST").split(",")
}

private fun String.toDownstreamApi() = DownstreamApi.parse(this)