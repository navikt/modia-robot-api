package no.nav.api.oppfolging

import no.nav.common.client.nom.CachedNomClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.health.HealthCheckResult
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.types.identer.NavIdent
import no.nav.utils.*
import okhttp3.OkHttpClient

class Nom(
    private val tokenclient: MachineToMachineTokenClient,
) {
    private val nomUrl = getRequiredProperty("NOM_URL")
    private val nomApi = DownstreamApi(
        cluster = "prod-gcp",
        namespace = "nom",
        application = "nom-api"
    )
    private val httpClient = OkHttpClient
        .Builder()
        .addInterceptor(XCorrelationIdInterceptor())
        .addInterceptor(
            LoggingInterceptor(
                name = "nom",
                callIdExtractor = { getCallId() }
            )
        )
        .build()

    val client: NomClient = if (isNotProd()) {
        DevMock
    } else {
        val tokenSupplier = { tokenclient.createMachineToMachineToken(nomApi) }
        CachedNomClient(NomClientImpl(nomUrl, tokenSupplier, httpClient))
    }

    object DevMock : NomClient {
        override fun finnNavn(navIdent: NavIdent): VeilederNavn = lagVeilederNavn(navIdent)
        override fun finnNavn(identer: List<NavIdent>): List<VeilederNavn> = identer.map(::lagVeilederNavn)
        override fun checkHealth(): HealthCheckResult = HealthCheckResult.healthy()

        private fun lagVeilederNavn(navIdent: NavIdent): VeilederNavn {
            val ident = navIdent.get()
            val identNr = ident.substring(1)
            return VeilederNavn()
                .setNavIdent(navIdent)
                .setFornavn("F_$identNr")
                .setEtternavn("E_$identNr")
                .setVisningsNavn("F_$identNr E_$identNr")
        }
    }

}