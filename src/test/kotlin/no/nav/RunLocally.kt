package no.nav

import io.mockk.mockk
import no.nav.api.oppfolging.OppfolgingService

fun main() {
    val services: Services = object : Services {
        override val oppfolgingService: OppfolgingService = mockk()
    }
    startApplication(
        disableSecurity = true,
        env = EnvImpl(),
        services = services,
    )
}
