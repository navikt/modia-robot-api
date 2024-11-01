package no.nav

import no.nav.api.dialog.saf.SafClient
import no.nav.api.dialog.sf.SFClient
import no.nav.api.digdir.DigdirClient
import no.nav.api.kontonummer.KontonummerRegister
import no.nav.api.oppfolging.Nom
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.api.pdl.PdlClient
import no.nav.api.skrivestotte.SkrivestotteClient
import no.nav.api.utbetalinger.UtbetalingerClient
import no.nav.common.client.nom.NomClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.utils.bindTo

interface Consumers {
    val tokenclient: MachineToMachineTokenClient
    val oboTokenClient: OnBehalfOfTokenClient
    val oppfolgingClient: OppfolgingClient
    val nom: NomClient
    val skrivestotteClient: SkrivestotteClient
    val pdlClient: PdlClient
    val safClient: SafClient
    val digdirClient: DigdirClient
    val kontonummerRegister: KontonummerRegister
    val utbetalingerClient: UtbetalingerClient
    val sfClient: SFClient
}

class ConsumersImpl(
    env: Env,
) : Consumers {
    override val oboTokenClient: OnBehalfOfTokenClient =
        AzureAdTokenClientBuilder
            .builder()
            .withNaisDefaults()
            .buildOnBehalfOfTokenClient()

    override val tokenclient: MachineToMachineTokenClient =
        AzureAdTokenClientBuilder
            .builder()
            .withNaisDefaults()
            .buildMachineToMachineTokenClient()

    override val oppfolgingClient: OppfolgingClient = OppfolgingClient(env.oppfolgingUrl, oboTokenClient.bindTo(env.oppfolgingScope))
    override val nom: NomClient = Nom(env.nomUrl, tokenclient.bindTo(env.nomScope)).client
    override val skrivestotteClient: SkrivestotteClient = SkrivestotteClient(env.skrivestotteUrl)
    override val pdlClient: PdlClient = PdlClient(env.pdlUrl, oboTokenClient.bindTo(env.pdlScope))
    override val safClient: SafClient = SafClient(env.safUrl, oboTokenClient.bindTo(env.safScope))
    override val digdirClient: DigdirClient =
        DigdirClient(env.digdirUrl, tokenclient.bindTo(env.digdirScope), oboTokenClient.bindTo(env.digdirScope))
    override val kontonummerRegister: KontonummerRegister =
        KontonummerRegister(env.kontonummerRegisterUrl, oboTokenClient.bindTo(env.kontonummerRegisterScope))
    override val utbetalingerClient: UtbetalingerClient =
        UtbetalingerClient(env.utbetalingSokosUrl, oboTokenClient.bindTo(env.utbetalingSokosScope))
    override val sfClient: SFClient = SFClient(env.sfUrl, oboTokenClient.bindTo(env.sfScope))
}
