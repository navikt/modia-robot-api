package no.nav.api.utbetalinger

import io.ktor.http.*
import kotlinx.datetime.LocalDate
import no.nav.common.cxf.StsConfig
import no.nav.plugins.WebStatusException
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.*
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest
import no.nav.utils.CXFClient
import no.nav.utils.externalServiceCall
import no.nav.utils.toJodaLocalDate
import javax.xml.namespace.QName

class UtbetalingerClient(
    utbetalingerUrl: String,
    stsConfig: StsConfig
) {
    
    val client: UtbetalingV1 = CXFClient<UtbetalingV1>()
        .wsdl("classpath:wsdl/utbetaling/no/nav/tjeneste/virksomhet/utbetaling/v1/Binding.wsdl")
        .serviceName(QName("http://nav.no/tjeneste/virksomhet/utbetaling/v1/Binding", "Utbetaling_v1"))
        .endpointName(QName("http://nav.no/tjeneste/virksomhet/utbetaling/v1/Binding", "Utbetaling_v1Port"))
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
        try {
            client.hentUtbetalingsinformasjon(request).utbetalingListe
        } catch (ex: Exception) {
            throw WebStatusException(
                message = ex.message ?: "Henting av utbetalinger for bruker med fnr $fnr mellom $fra og $til feilet.",
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    suspend fun ping() = externalServiceCall {
        client.ping()
    }
}
