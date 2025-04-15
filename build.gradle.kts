import com.expediagroup.graphql.plugin.gradle.config.GraphQLScalar
import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLDownloadSDLTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

val ktor_version = "3.0.3"
val kotlin_version = "2.0.21"
val kotlinx_datetime_version = "0.6.2"
val kompendium_version = "4.0.3"
val logback_version = "1.5.18"
val logstash_version = "8.1"
val prometeus_version = "1.14.6"
val nav_common_version = "3.2025.03.25_13.00-69496eec5820"
val tjenestespec_version = "1.2021.02.22-10.45-4201aaea72fb"
val modia_common_utils_version = "1.2025.04.10-08.38-f3f297ca275e"
val junit_version = "5.12.2"
val graphql_kotlin_version = "8.6.0"

plugins {
    application
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
    id("com.gradleup.shadow") version "8.3.6"
    id("com.expediagroup.graphql") version "8.5.0"
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
    implementation("no.nav.common:token-client:$nav_common_version")
    implementation("no.nav.common:client:$nav_common_version")
    implementation("no.nav.common:log:$nav_common_version")
    implementation("org.slf4j:jul-to-slf4j:2.0.17")
    implementation("com.sun.xml.ws:jaxws-ri:4.0.3")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstash_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphql_kotlin_version")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
    testImplementation("io.mockk:mockk:1.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        // This is for logging and can be removed.
        events("passed", "skipped", "failed")
    }
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
    transform(ServiceFileTransformer::class.java) {
        setPath("META-INF/cxf")
        include("bus-extensions.txt")
    }
}

val downloadSAFSchema by tasks.creating(GraphQLDownloadSDLTask::class) {
    endpoint.set("https://navikt.github.io/saf/saf-api-sdl.graphqls")
    outputFile.set(file("${project.projectDir}/src/main/resources/saf/schema.graphqls"))
}
val generateSAFClient by tasks.creating(GraphQLGenerateClientTask::class) {
    packageName.set("no.nav.api.generated.saf")
    schemaFile.set(downloadSAFSchema.outputFile)
    queryFiles.from(fileTree("${project.projectDir}/src/main/resources/saf/queries/").files)
    serializer.set(GraphQLSerializer.KOTLINX)
    dependsOn("downloadSAFSchema")
}

val downloadPDLSchema by tasks.creating(GraphQLDownloadSDLTask::class) {
    endpoint.set("https://navikt.github.io/pdl/pdl-api-sdl.graphqls")
    outputFile.set(file("${project.projectDir}/src/main/resources/pdl/schema.graphqls"))
    dependsOn("generateSAFClient")
}
val generatePDLClient by tasks.creating(GraphQLGenerateClientTask::class) {
    packageName.set("no.nav.api.generated.pdl")
    schemaFile.set(downloadPDLSchema.outputFile)
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

tasks {
    processResources {
        dependsOn("generatePDLClient")
    }
}
