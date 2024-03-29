package no.nav

import no.nav.api.dialog.saf.SafClient
import no.nav.api.dialog.sf.SFClient
import no.nav.api.digdir.DigdirClient
import no.nav.api.oppfolging.Nom
import no.nav.api.oppfolging.OppfolgingClient
import no.nav.api.pdl.PdlClient
import no.nav.api.skrivestotte.SkrivestotteClient
import no.nav.api.utbetalinger.UtbetalingerClient
import no.nav.common.client.nom.NomClient
import no.nav.common.cxf.StsConfig
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.common.utils.NaisUtils
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.utils.CXFClient
import no.nav.utils.bindTo

interface Consumers {
    val tokenclient: MachineToMachineTokenClient
    val oboTokenClient: OnBehalfOfTokenClient
    val oppfolgingClient: OppfolgingClient
    val tps: PersonV3
    val nom: NomClient
    val skrivestotteClient: SkrivestotteClient
    val pdlClient: PdlClient
    val safClient: SafClient
    val digdirClient: DigdirClient
    val utbetalingerClient: UtbetalingerClient
    val sfClient: SFClient
}

class ConsumersImpl(env: Env) : Consumers {
    private val modiaUser = NaisUtils.getCredentials("service_user")
    private val stsConfig: StsConfig =
        StsConfig
            .builder()
            .url(env.soapStsUrl)
            .username(modiaUser.username)
            .password(modiaUser.password)
            .build()

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
    override val tps: PersonV3 =
        CXFClient<PersonV3>()
            .address(env.tpsPersonV3Url)
            .configureStsForSystemUser(stsConfig)
            .build()
    override val nom: NomClient = Nom(env.nomUrl, tokenclient.bindTo(env.nomScope)).client
    override val skrivestotteClient: SkrivestotteClient = SkrivestotteClient(env.skrivestotteUrl)
    override val pdlClient: PdlClient = PdlClient(env.pdlUrl, oboTokenClient.bindTo(env.pdlScope))
    override val safClient: SafClient = SafClient(env.safUrl, oboTokenClient.bindTo(env.safScope))
    override val digdirClient: DigdirClient =
        DigdirClient(env.digdirUrl, tokenclient.bindTo(env.digdirScope), oboTokenClient.bindTo(env.digdirScope))
    override val utbetalingerClient: UtbetalingerClient =
        UtbetalingerClient(env.utbetalingSokosUrl, oboTokenClient.bindTo(env.utbetalingSokosScope))
    override val sfClient: SFClient = SFClient(env.sfUrl, oboTokenClient.bindTo(env.sfScope))
}
