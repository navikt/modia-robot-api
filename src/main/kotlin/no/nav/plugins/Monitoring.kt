package no.nav.plugins

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.prometheus.*
import no.nav.personoversikt.utils.SelftestGenerator
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
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    val selftest = SelftestGenerator.getInstance(SelftestGenerator.Config(
        appname = "modia-robot-api",
        version = appImage
    ))
    routing {
        get {
            call.respondRedirect("/swagger-ui")
        }
        route("internal") {
            route("isAlive") {
                notarizedGet(Api.isAlive) {
                    if (selftest.isAlive()) {
                        call.respondText("Alive")
                    } else {
                        call.respondText("Not alive", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
            route("isReady") {
                notarizedGet(Api.isReady) {
                    if (selftest.isReady()) {
                        call.respondText("Ready")
                    } else {
                        call.respondText("Not ready", status = HttpStatusCode.InternalServerError)
                    }
                }
            }

            get("metrics") {
                call.respond(appMicrometerRegistry.scrape())
            }

            get("selftest") {
                call.respondText(selftest.scrape())
            }
        }
    }
}
private object Api {
    val isAlive = GetInfo<Unit, String>(
        summary = "isAlive health probe",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "App is alive"
        ),
        tags = setOf("Monitoring"),
    )
    val isReady = GetInfo<Unit, String>(
        summary = "isReady health probe",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "App is ready"
        ),
        tags = setOf("Monitoring"),
    )
}
