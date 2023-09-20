package no.nav.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.personoversikt.common.ktor.utils.Metrics
import no.nav.personoversikt.common.utils.SelftestGenerator
import no.nav.utils.appImage
import no.nav.utils.masked
import org.slf4j.event.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            call.request.path().contains("/internal/").not()
        }
        callIdMdc("call-id")
        format { call ->
            "${call.response.status()}: ${call.request.httpMethod} - ${call.request.origin.uri.substringBefore("?")}".masked()
        }
    }
    install(CallId) {
        header(HttpHeaders.XCorrelationId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

    install(Metrics.Plugin)

    val selftest = SelftestGenerator.getInstance(
        SelftestGenerator.Config(
            appname = "modia-robot-api",
            version = appImage,
        ),
    )
    routing {
        get {
            call.respondRedirect("/swagger-ui")
        }
        route("internal") {
            route("isAlive") {
                get {
                    if (selftest.isAlive()) {
                        call.respondText("Alive")
                    } else {
                        call.respondText("Not alive", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
            route("isReady") {
                get {
                    if (selftest.isReady()) {
                        call.respondText("Ready")
                    } else {
                        call.respondText("Not ready", status = HttpStatusCode.InternalServerError)
                    }
                }
            }

            get("selftest") {
                call.respondText(selftest.scrape())
            }
        }
    }
}
