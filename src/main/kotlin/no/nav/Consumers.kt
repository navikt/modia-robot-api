package no.nav

import no.nav.api.oppfolging.Nom
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.api.skrivestotte.SkrivestotteClient
import no.nav.common.client.nom.NomClient
import no.nav.common.cxf.StsConfig
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.utils.NaisUtils
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.utils.CXFClient

interface Consumers {
    val oppfolgingClient: OppfolgingClient
    val tps: PersonV3
    val nom: NomClient
    val skrivestotteClient: SkrivestotteClient
}

class ConsumersImpl(env: Env) : Consumers {
    private val modiaUser = NaisUtils.getCredentials("service_user")
    private val stsConfig: StsConfig = StsConfig
        .builder()
        .url(env.soapStsUrl)
        .username(modiaUser.username)
        .password(modiaUser.password)
        .build()

    private val tokenclient: MachineToMachineTokenClient = AzureAdTokenClientBuilder
        .builder()
        .withNaisDefaults()
        .buildMachineToMachineTokenClient()

    override val oppfolgingClient: OppfolgingClient = OppfolgingClient(env.oppfolgingUrl, tokenclient)
    override val tps: PersonV3 = CXFClient<PersonV3>()
        .address(env.tpsPersonV3Url)
        .configureStsForSystemUser(stsConfig)
        .build()
    override val nom: NomClient = Nom(env.nomUrl, tokenclient).client
    override val skrivestotteClient: SkrivestotteClient = SkrivestotteClient(env.skrivestotteUrl)
}
