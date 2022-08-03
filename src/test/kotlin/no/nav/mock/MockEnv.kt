package no.nav.mock

import no.nav.Env

object MockEnv : Env {
    override val soapStsUrl: String = ""
    override val jwksUrl: String = ""
    override val oppfolgingUrl: String = ""
    override val tpsPersonV3Url: String = ""
    override val nomUrl: String = ""
    override val pdlUrl: String = ""
    override val safUrl: String = ""
    override val identAllowList: List<String> = emptyList()
    override val skrivestotteUrl: String = ""
}