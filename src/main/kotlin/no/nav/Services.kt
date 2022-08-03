package no.nav

import no.nav.api.dialog.DialogService
import no.nav.api.dialog.saf.SafService
import no.nav.api.oppfolging.OppfolgingService
import no.nav.api.pdl.PdlService
import no.nav.api.skrivestotte.SkrivestotteService
import no.nav.api.tps.TpsService

interface Services {
    val oppfolgingService: OppfolgingService
    val tpsService: TpsService
    val skrivestotteService: SkrivestotteService
    val pdlService: PdlService
    val safService: SafService
    val dialogService: DialogService
}
class ServicesImpl(consumers: Consumers) : Services {
    override val oppfolgingService = OppfolgingService(
        consumers.oppfolgingClient,
        consumers.nom
    )
    override val tpsService = TpsService(consumers.tps)
    override val skrivestotteService = SkrivestotteService(consumers.skrivestotteClient)
    override val pdlService = PdlService(consumers.pdlClient)
    override val safService = SafService(consumers.safClient)
    override val dialogService = DialogService(safService)
}
