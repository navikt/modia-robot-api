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
import no.nav.utils.masked
import org.slf4j.event.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
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

    routing {
        get {
            call.respondRedirect("/swagger-ui")
        }
        route("internal") {
            route("isAlive") {
                notarizedGet(Api.isAlive) { call.respondText("Alive") }
            }
            route("isReady") {
                notarizedGet(Api.isReady) { call.respondText("Ready") }
                get("isReady") { call.respondText("Ready") }
            }
            route("metrics") {
                notarizedGet(Api.metrics) {
                    call.respond(appMicrometerRegistry.scrape())
                }
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
    val metrics = GetInfo<Unit, String>(
        summary = "metrics exporter",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = ""
        ),
        tags = setOf("Monitoring"),
    )
}
