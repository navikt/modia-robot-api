package no.nav

import no.nav.api.oppfolging.OppfolgingService
import no.nav.api.tps.TpsService
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder

interface Services {
    val oppfolgingService: OppfolgingService
    val tpsService: TpsService
}
class ServicesImpl(consumers: Consumers) : Services {
    override val oppfolgingService = OppfolgingService(
        consumers.oppfolgingClient,
        consumers.nom
    )
    override val tpsService = TpsService(consumers.tps)
}
