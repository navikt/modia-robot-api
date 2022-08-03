package no.nav.api.utbetalinger

import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1

class UtbetalingerService(
    val utbetalinger: UtbetalingV1
) {
    suspend fun hentUtbetalinger() {
        TODO()
    }
}