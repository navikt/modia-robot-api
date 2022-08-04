package no.nav

import no.nav.api.digdir.DigdirService
import no.nav.api.oppfolging.OppfolgingService
import no.nav.api.pdl.PdlService
import no.nav.api.skrivestotte.SkrivestotteService
import no.nav.api.tps.TpsService

interface Services {
    val oppfolgingService: OppfolgingService
    val tpsService: TpsService
    val skrivestotteService: SkrivestotteService
    val pdlServices: PdlService
    val digdirService: DigdirService
}
class ServicesImpl(consumers: Consumers) : Services {
    override val oppfolgingService = OppfolgingService(
        consumers.oppfolgingClient,
        consumers.nom
    )
    override val tpsService = TpsService(consumers.tps)
    override val skrivestotteService = SkrivestotteService(consumers.skrivestotteClient)
    override val pdlServices = PdlService(consumers.pdlClient)
    override val digdirService = DigdirService(consumers.digdirClient)

}
