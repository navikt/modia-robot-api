package no.nav

import no.nav.personoversikt.common.utils.EnvUtils.getRequiredConfig
import no.nav.utils.DownstreamApi
import no.nav.utils.parse

interface Env {
    companion object {
        operator fun invoke(): Env = EnvImpl()
    }

    val jwksUrl: String
    val oppfolgingUrl: String
    val oppfolgingScope: DownstreamApi
    val syfoUrl: String
    val syfoScope: DownstreamApi
    val nomUrl: String
    val nomScope: DownstreamApi
    val pdlUrl: String
    val pdlScope: DownstreamApi
    val digdirUrl: String
    val digdirScope: DownstreamApi
    val kontonummerRegisterUrl: String
    val kontonummerRegisterScope: DownstreamApi
    val utbetalingSokosUrl: String
    val utbetalingSokosScope: DownstreamApi
    val safUrl: String
    val safScope: DownstreamApi
    val skrivestotteUrl: String
    val sfUrl: String
    val sfScope: DownstreamApi
    val identAllowList: List<String>
}

class EnvImpl : Env {
    override val jwksUrl: String = getRequiredConfig("AZURE_OPENID_CONFIG_JWKS_URI")
    override val oppfolgingUrl: String = getRequiredConfig("OPPFOLGING_URL")
    override val oppfolgingScope: DownstreamApi = getRequiredConfig("OPPFOLGING_SCOPE").toDownstreamApi()
    override val syfoUrl: String = getRequiredConfig("SYFO_URL")
    override val syfoScope: DownstreamApi = getRequiredConfig("SYFO_SCOPE").toDownstreamApi()
    override val nomUrl: String = getRequiredConfig("NOM_URL")
    override val nomScope: DownstreamApi = getRequiredConfig("NOM_SCOPE").toDownstreamApi()
    override val pdlUrl: String = getRequiredConfig("PDL_URL")
    override val pdlScope: DownstreamApi = getRequiredConfig("PDL_SCOPE").toDownstreamApi()
    override val digdirUrl: String = getRequiredConfig("DIGDIR_KRR_URL")
    override val digdirScope: DownstreamApi = getRequiredConfig("DIGDIR_KRR_SCOPE").toDownstreamApi()
    override val kontonummerRegisterUrl: String = getRequiredConfig("KONTOREGISTER_REST_URL")
    override val kontonummerRegisterScope: DownstreamApi = getRequiredConfig("KONTOREGISTER_SCOPE").toDownstreamApi()
    override val utbetalingSokosUrl: String = getRequiredConfig("UTBETALDATA_SOKOS_URL")
    override val utbetalingSokosScope: DownstreamApi = getRequiredConfig("UTBETAL_SOKOS_SCOPE").toDownstreamApi()
    override val safUrl: String = getRequiredConfig("SAF_URL")
    override val safScope: DownstreamApi = getRequiredConfig("SAF_SCOPE").toDownstreamApi()
    override val skrivestotteUrl: String = getRequiredConfig("SKRIVESTOTTE_URL")
    override val sfUrl: String = getRequiredConfig("SF_HENVENDELSE_URL")
    override val sfScope: DownstreamApi = getRequiredConfig("SF_HENVENDELSE_SCOPE").toDownstreamApi()
    override val identAllowList: List<String> = getRequiredConfig("IDENT_ALLOW_LIST").uppercase().split(",")
}

private fun String.toDownstreamApi() = DownstreamApi.parse(this)
