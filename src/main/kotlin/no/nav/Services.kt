package no.nav

import no.nav.api.digdir.DigdirService
import no.nav.api.dialog.DialogService
import no.nav.api.dialog.saf.SafService
import no.nav.api.dialog.sf.SFService
import no.nav.api.oppfolging.OppfolgingService
import no.nav.api.pdl.PdlService
import no.nav.api.skrivestotte.SkrivestotteService
import no.nav.api.tps.TpsService
import no.nav.api.utbetalinger.UtbetalingerService

interface Services {
    val oppfolgingService: OppfolgingService
    val tpsService: TpsService
    val skrivestotteService: SkrivestotteService
    val digdirService: DigdirService
    val pdlService: PdlService
    val safService: SafService
    val sfService: SFService
    val dialogService: DialogService
    val utbetalingerService: UtbetalingerService
}
class ServicesImpl(consumers: Consumers) : Services {
    override val oppfolgingService = OppfolgingService(
        consumers.oppfolgingClient,
        consumers.nom
    )
    override val tpsService = TpsService(consumers.tps)
    override val skrivestotteService = SkrivestotteService(consumers.skrivestotteClient)
    override val digdirService = DigdirService(consumers.digdirClient)
    override val pdlService = PdlService(consumers.pdlClient)
    override val safService = SafService(consumers.safClient)
    override val sfService = SFService(consumers.sfClient)
    override val dialogService = DialogService(safService, sfService, pdlService)
    override val utbetalingerService = UtbetalingerService(consumers.utbetalingerClient)
}
