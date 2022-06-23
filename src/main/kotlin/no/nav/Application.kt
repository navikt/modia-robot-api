package no.nav

import io.bkbn.kompendium.auth.Notarized.notarizedAuthenticate
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.api.oppfolging.configureOppfolgingRoutes
import no.nav.plugins.*

fun startApplication(disableSecurity: Boolean) {
    val env = Env()
    val services = Services(env)

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        environment.config
        configureOpenApi()
        configureSecurity(disableSecurity)
        configureMonitoring()
        configureSerialization()

        routing {
            notarizedAuthenticate(securityScheme) {
                configureOppfolgingRoutes(services.oppfolgingService)
            }
        }
    }.start(wait = true)
}

fun main() {
    startApplication(disableSecurity = false)
}
