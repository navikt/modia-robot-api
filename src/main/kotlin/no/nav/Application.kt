package no.nav

import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import no.nav.api.debug.configureDebugRoutes
import no.nav.api.dialog.configureDialogRoutes
import no.nav.api.digdir.configureDigdirRoutes
import no.nav.api.kontonummer.configureKontonummerRegisterRoutes
import no.nav.api.oppfolging.configureOppfolgingRoutes
import no.nav.api.pdl.configurePdlRoutes
import no.nav.api.skrivestotte.configureSkrivestotteRoutes
import no.nav.api.syfo.configureSyfoRoutes
import no.nav.api.utbetalinger.configureUtbetalingerRoutes
import no.nav.plugins.*

fun startApplication(
    disableSecurity: Boolean,
    env: Env = Env(),
    consumers: Consumers = ConsumersImpl(env),
    services: Services = ServicesImpl(consumers),
) {
    embeddedServer(Netty, port = 7070, host = "0.0.0.0") {
        environment.config
        configureOpenApi()
        configureSecurity(disableSecurity, env)
        configureMonitoring()
        configureSerialization()
        configureExceptionHandling()

        routing {
            authenticate(SECURITY_SCHEME_NAME) {
                route("api") {
                    configureDebugRoutes(consumers.tokenclient)
                    configureOppfolgingRoutes(services.oppfolgingService)
                    configurePdlRoutes(services.pdlService)
                    configureKontonummerRegisterRoutes(consumers.kontonummerRegister)
                    configureDialogRoutes(services.dialogService)
                    configureDigdirRoutes(services.digdirService)
                    configureSkrivestotteRoutes(services.skrivestotteService)
                    configureUtbetalingerRoutes(services.utbetalingerService)
                    configureSyfoRoutes(services.syfoService)
                }
            }
        }
    }.start(wait = true)
}

fun main() {
    startApplication(disableSecurity = false)
}
