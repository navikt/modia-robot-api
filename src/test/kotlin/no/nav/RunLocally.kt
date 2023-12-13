package no.nav

import no.nav.mock.MockConsumers
import no.nav.mock.MockEnv

fun main() {
    System.setProperty("IDENT_ALLOW_LIST", "")
    startApplication(
        disableSecurity = true,
        env = MockEnv,
        consumers = MockConsumers,
    )
}
