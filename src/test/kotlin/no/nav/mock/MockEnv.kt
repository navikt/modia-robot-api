package no.nav.mock

import no.nav.Env
import no.nav.utils.DownstreamApi
import no.nav.utils.parse

object MockEnv : Env {
    override val soapStsUrl: String = ""
    override val jwksUrl: String = ""
    override val oppfolgingUrl: String = ""
    override val oppfolgingScope: DownstreamApi = DownstreamApi.parse("::")
    override val tpsPersonV3Url: String = ""
    override val nomUrl: String = ""
    override val nomScope: DownstreamApi = DownstreamApi.parse("::")
    override val identAllowList: List<String> = emptyList()
    override val skrivestotteUrl: String = ""
    override val digdirUrl: String = ""
    override val digdirScope: DownstreamApi = DownstreamApi.parse("::")
    override val utbetalingerUrl: String = ""
}