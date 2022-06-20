package no.nav

import no.nav.api.oppfolging.OppfolgingService
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder

class Services(env: Env) : Env by env {
    val tokenclient = AzureAdTokenClientBuilder
        .builder()
        .withNaisDefaults()
        .buildMachineToMachineTokenClient()

    val oppfolgingService = OppfolgingService(tokenclient)
}
