import com.expediagroup.graphql.plugin.gradle.config.GraphQLScalar
import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLDownloadSDLTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask

val ktor_version = "3.5.2"
val kotlin_version = "2.0.21"
val kotlinx_datetime_version = "0.8.0"
val kompendium_version = "4.0.3"
val logback_version = "1.6.3"
val logstash_version = "9.0"
val prometeus_version = "1.17.1"
val nav_common_version = "4.2026.09.01_09.49-e681e09ca089"
val tjenestespec_version = "1.2021.02.22-10.45-4201aaea72fb"
val modia_common_utils_version = "1.2026.08.06-12.11-d922f6248916"
val junit_version = "6.1.3"
val graphql_kotlin_version = "10.2.2"
val swagger_ui_version = "5.25.3"

plugins {
    application
    kotlin("jvm") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10"
    id("com.expediagroup.graphql") version "10.2.2"
    id("org.openapi.generator") version "7.25.0"
}

group = "no.nav"
version = "0.0.1"

application {
    mainClass.set("no.nav.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()

    val githubToken = System.getenv("GITHUB_TOKEN")
    if (githubToken.isNullOrEmpty()) {
        maven {
            name = "external-mirror-github-navikt"
            url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        }
    } else {
        maven {
            name = "github-package-registry-navikt"
            url = uri("https://maven.pkg.github.com/navikt/maven-release")
            credentials {
                username = "token"
                password = githubToken
            }
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-server:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-swagger-jvm:$ktor_version")
    implementation("org.webjars:swagger-ui:$swagger_ui_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_datetime_version")
    implementation("io.bkbn:kompendium-core:$kompendium_version")
    implementation("no.nav.tjenestespesifikasjoner:person-v3-tjenestespesifikasjon:$tjenestespec_version")
    implementation("no.nav.tjenestespesifikasjoner:utbetaling-tjenestespesifikasjon:$tjenestespec_version")
    implementation("com.github.navikt.modia-common-utils:kotlin-utils:$modia_common_utils_version")
    implementation("com.github.navikt.modia-common-utils:ktor-utils:$modia_common_utils_version")
    implementation("com.github.navikt.modia-common-utils:logging:$modia_common_utils_version")
    implementation("no.nav.common:token-client:$nav_common_version")
    implementation("no.nav.common:client:$nav_common_version")
    implementation("no.nav.common:log:$nav_common_version")
    implementation("org.slf4j:jul-to-slf4j:2.0.19")
    implementation("com.sun.xml.ws:jaxws-ri:4.0.5")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstash_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphql_kotlin_version")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        // This is for logging and can be removed.
        events("passed", "skipped", "failed")
    }
}

val downloadSAFSchema =
    tasks.register<GraphQLDownloadSDLTask>("downloadSAFSchema") {
        endpoint.set("https://navikt.github.io/saf/saf-api-sdl.graphqls")
        outputFile.set(file("${project.projectDir}/src/main/resources/saf/schema.graphqls"))
    }

tasks.register<GraphQLGenerateClientTask>("generateSAFClient") {
    packageName.set("no.nav.api.generated.saf")
    schemaFile.set(downloadSAFSchema.flatMap { it.outputFile })
    queryFiles.from(fileTree("${project.projectDir}/src/main/resources/saf/queries/").files)
    serializer.set(GraphQLSerializer.KOTLINX)
    dependsOn(downloadSAFSchema)
}

val downloadPDLSchema =
    tasks.register<GraphQLDownloadSDLTask>("downloadPDLSchema") {
        endpoint.set("https://navikt.github.io/pdl/pdl-api-sdl.graphqls")
        outputFile.set(file("${project.projectDir}/src/main/resources/pdl/schema.graphqls"))
        dependsOn("generateSAFClient")
    }

tasks.register<GraphQLGenerateClientTask>("generatePDLClient") {
    packageName.set("no.nav.api.generated.pdl")
    schemaFile.set(downloadPDLSchema.flatMap { it.outputFile })
    queryFiles.from(fileTree("${project.projectDir}/src/main/resources/pdl/queries/").files)
    serializer.set(GraphQLSerializer.KOTLINX)
    customScalars.add(
        GraphQLScalar(
            "Long",
            "no.nav.api.pdl.converters.PdlLong",
            "no.nav.api.pdl.converters.LongScalarConverter",
        ),
    )
    customScalars.add(
        GraphQLScalar(
            "Date",
            "kotlinx.datetime.LocalDate",
            "no.nav.api.pdl.converters.DateScalarConverter",
        ),
    )
    customScalars.add(
        GraphQLScalar(
            "DateTime",
            "kotlinx.datetime.LocalDateTime",
            "no.nav.api.pdl.converters.DateTimeScalarConverter",
        ),
    )
    dependsOn("downloadPDLSchema")
}

val generatedSourcesPath = layout.buildDirectory.dir("generated/source/openapi")

openApiGenerate {
    inputSpec.set("${project.projectDir}/src/main/resources/kodeverk/openapi.json")
    outputDir.set(generatedSourcesPath)
    generatorName.set("kotlin")
    packageName.set("no.nav.api.generated.kodeverk")
    configOptions.set(
        mapOf(
            "library" to "jvm-ktor",
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "kotlinx-datetime",
        ),
    )
}

tasks {
    register("generateApi") {
        group = "build"
        description = "Generate API"
        dependsOn("openApiGenerate")
    }
    processResources {
        dependsOn("generateApi", "generatePDLClient")
    }
    compileKotlin {
        dependsOn("generateApi")
        mustRunAfter("generatePDLClient")
    }
}

sourceSets {
    main {
        kotlin {
            srcDir(generatedSourcesPath.map { it.dir("src/main/kotlin") })
        }
    }
}
