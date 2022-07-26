package no.nav

import no.nav.api.oppfolging.Nom
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.common.client.nom.NomClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.utils.CXFClient

interface  Consumers {
    val oppfolgingClient: OppfolgingClient
    val tps: PersonV3
    val nom: NomClient
}

class ConsumersImpl(env: Env): Consumers {
    private val tokenclient: MachineToMachineTokenClient = AzureAdTokenClientBuilder
        .builder()
        .withNaisDefaults()
        .buildMachineToMachineTokenClient()

    override val oppfolgingClient: OppfolgingClient = OppfolgingClient(tokenclient)
    override val tps: PersonV3 = CXFClient<PersonV3>()
        .address(env.tpsPersonV3Url)
        .build()
    override val nom: NomClient = Nom(tokenclient).client

}