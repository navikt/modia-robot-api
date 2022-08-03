package no.nav.plugins.swaggerui

import io.bkbn.kompendium.swagger.JsConfig
import io.ktor.features.NotFoundException
import io.ktor.http.*
import io.ktor.http.content.ByteArrayContent
import java.net.URL
import org.webjars.WebJarAssetLocator
import kotlin.reflect.full.memberProperties

internal fun WebJarAssetLocator.getSwaggerResource(path: String): URL =
    this::class.java.getResource(getFullPath("swagger-ui", path).let { if (it.startsWith("/")) it else "/$it" })
        ?: throw NotFoundException("Resource not found: $path")

internal fun WebJarAssetLocator.getSwaggerResourceContent(path: String): ByteArrayContent =
    ByteArrayContent(
        bytes = getSwaggerResource(path).readBytes(),
        contentType = ContentType.defaultForFilePath(path)
    )

internal fun WebJarAssetLocator.getSwaggerInitializerContent(jsConfig: JsConfig): ByteArrayContent = ByteArrayContent(
    getSwaggerResource(path = "swagger-initializer.js").readText()
        .replaceFirst("url: \"https://petstore.swagger.io/v2/swagger.json\",", "urls: ${jsConfig.getSpecUrlsProps()},")
        .replaceFirst("deepLinking: true", jsConfig.toJsProps())
        .let { content ->
            jsConfig.jsInit()?.let {
                content.replaceFirst("});", "});\n$it")
            } ?: content
        }.toByteArray()
)

internal inline fun<reified T: Any> T.asMap(): Map<String, Any?> =
    T::class.memberProperties.associate { it.name to it.get(this) }

internal fun JsConfig.toJsProps(): String = asMap()
    .filterKeys { !setOf("specs", "jsInit").contains(it) }
    .map{ "${it.key}: ${it.value.toJs()}" }
    .joinToString(separator = ",\n    ")

internal fun JsConfig.getSpecUrlsProps(): String =
    if (specs.isEmpty()) "[]" else specs.map { "{url: ${it.value.toJs()}, name: ${it.key.toJs()}}" }.toString()