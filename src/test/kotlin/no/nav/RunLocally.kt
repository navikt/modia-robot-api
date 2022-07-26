package no.nav

import no.nav.mock.MockConsumers

fun main() {
    System.setProperty("IDENT_ALLOW_LIST", "")
    startApplication(
        disableSecurity = true,
        env = EnvImpl(),
        consumers = MockConsumers,
    )
}
