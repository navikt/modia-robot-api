package no.nav.plugins

import io.bkbn.kompendium.core.Kompendium
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.swagger.JsConfig
import io.bkbn.kompendium.swagger.SwaggerUI
import io.ktor.application.*
import java.net.URI

fun Application.configureOpenApi() {
    install(Kompendium) {
        spec = OpenApiSpec(
            info = Info(
                "modia-robot-api",
                version = "1.0.0"
            )
        )
    }

    install(SwaggerUI) {
        jsConfig = JsConfig(
            specs = mapOf(
                "Version 1" to URI("/openapi.json")
            )
        )
    }
}
