package no.nav

import io.bkbn.kompendium.auth.Notarized.notarizedAuthenticate
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.api.dialog.configureDialogRoutes
import no.nav.api.digdir.configureDigdirRoutes
import no.nav.api.oppfolging.configureOppfolgingRoutes
import no.nav.api.pdl.configurePdlRoutes
import no.nav.api.skrivestotte.configureSkrivestotteRoutes
import no.nav.api.tps.configureTpsRoutes
import no.nav.plugins.*

fun startApplication(
    disableSecurity: Boolean,
    env: Env = Env(),
    services: Services = ServicesImpl(env)
) {
    embeddedServer(Netty, port = 7070, host = "0.0.0.0") {
        environment.config
        configureOpenApi()
        configureSecurity(disableSecurity, "")
        configureMonitoring()
        configureSerialization()

        routing {
            notarizedAuthenticate(securityScheme) {
                route("api") {
                    configureOppfolgingRoutes(services.oppfolgingService)
                    configurePdlRoutes()
                    configureTpsRoutes()
                    configureDialogRoutes()
                    configureDigdirRoutes()
                    configureSkrivestotteRoutes()
                }
            }
        }
    }.start(wait = true)
}

fun main() {
    startApplication(disableSecurity = true)
}
