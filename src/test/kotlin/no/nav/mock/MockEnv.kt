package no.nav.mock

import no.nav.Env
import no.nav.utils.DownstreamApi
import no.nav.utils.parse

object MockEnv : Env {
    override val jwksUrl: String = ""
    override val kontonummerRegisterUrl: String = ""
    override val kontonummerRegisterScope = DownstreamApi.parse("::")
    override val oppfolgingUrl: String = ""
    override val oppfolgingScope: DownstreamApi = DownstreamApi.parse("::")
    override val syfoUrl: String = ""
    override val syfoScope: DownstreamApi = DownstreamApi.parse("::")
    override val nomUrl: String = ""
    override val nomScope: DownstreamApi = DownstreamApi.parse("::")
    override val pdlUrl: String = ""
    override val pdlScope: DownstreamApi = DownstreamApi.parse("::")
    override val digdirUrl: String = ""
    override val digdirScope: DownstreamApi = DownstreamApi.parse("::")
    override val utbetalingSokosUrl: String = ""
    override val utbetalingSokosScope: DownstreamApi = DownstreamApi.parse("::")
    override val safUrl: String = ""
    override val safScope: DownstreamApi = DownstreamApi.parse("::")
    override val identAllowList: List<String> = emptyList()
    override val skrivestotteUrl: String = ""
    override val sfUrl: String = ""
    override val sfScope: DownstreamApi = DownstreamApi.parse("::")
}
