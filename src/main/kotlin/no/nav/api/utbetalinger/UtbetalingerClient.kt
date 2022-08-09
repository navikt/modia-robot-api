package no.nav.api.utbetalinger

import kotlinx.datetime.LocalDate
import no.nav.common.cxf.StsConfig
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.*
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest
import no.nav.utils.CXFClient
import no.nav.utils.externalServiceCall
import no.nav.utils.toJodaLocalDate

class UtbetalingerClient(
    utbetalingerUrl: String,
    stsConfig: StsConfig
) {
    
    val client: UtbetalingV1 = CXFClient<UtbetalingV1>()
        .address(utbetalingerUrl)
        .configureStsForSystemUser(stsConfig)
        .build()

    suspend fun hentUtbetalinger(fnr: String, fra: LocalDate, til: LocalDate): List<WSUtbetaling> = externalServiceCall {
        val request = WSHentUtbetalingsinformasjonRequest()
            .withId(
                WSIdent()
                    .withIdent(fnr)
                    .withIdentType(WSIdenttyper().withValue("Personnr"))
                    .withRolle(WSIdentroller().withValue("Rettighetshaver"))
            )
            .withPeriode(
                WSForespurtPeriode()
                    .withFom(fra.toJodaLocalDate().toDateTimeAtStartOfDay())
                    .withTom(til.toJodaLocalDate().toDateTimeAtStartOfDay())
            )
        client.hentUtbetalingsinformasjon(request).utbetalingListe
    }

    suspend fun ping() = externalServiceCall {
        client.ping()
    }
}
