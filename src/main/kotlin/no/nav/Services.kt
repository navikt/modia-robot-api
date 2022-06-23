package no.nav

import no.nav.api.oppfolging.OppfolgingService
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder

interface Services {
    val oppfolgingService: OppfolgingService
}
class ServicesImpl(env: Env) : Services, Env by env {
    val tokenclient = AzureAdTokenClientBuilder
        .builder()
        .withNaisDefaults()
        .buildMachineToMachineTokenClient()

    override val oppfolgingService = OppfolgingService(tokenclient)
}
