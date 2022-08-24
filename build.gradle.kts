import com.expediagroup.graphql.plugin.gradle.config.GraphQLScalar
import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLDownloadSDLTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

val ktor_version: String by project
val kotlin_version: String by project
val kotlinx_datetime_version: String by project
val kompendium_version: String by project
val logback_version: String by project
val logstash_version: String by project
val prometeus_version: String by project
val nav_common_version: String by project
val tjenestespec_version: String by project
val modia_common_utils_version: String by project
val junit_version: String by project
val graphql_kotlin_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.expediagroup.graphql") version "5.5.0"
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
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_datetime_version")
    implementation("io.bkbn:kompendium-core:$kompendium_version")
    implementation("io.bkbn:kompendium-auth:$kompendium_version")
    implementation("io.bkbn:kompendium-swagger-ui:$kompendium_version")
    implementation("org.webjars:webjars-locator-core:0.50")
    implementation("no.nav.tjenestespesifikasjoner:person-v3-tjenestespesifikasjon:$tjenestespec_version")
    implementation("no.nav.tjenestespesifikasjoner:utbetaling-tjenestespesifikasjon:$tjenestespec_version")
    implementation("no.nav.personoversikt:kotlin-utils:$modia_common_utils_version")

    implementation("no.nav.common:token-client:$nav_common_version")
    implementation("no.nav.common:cxf:$nav_common_version")
    implementation("no.nav.common:client:$nav_common_version")
    implementation("com.sun.xml.ws:jaxws-ri:2.3.3")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstash_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphql_kotlin_version")


    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {     // This is for logging and can be removed.
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
            "no.nav.api.pdl.converters.LongScalarConverter"
        )
    )
    customScalars.add(
        GraphQLScalar(
            "Date",
            "kotlinx.datetime.LocalDate",
            "no.nav.api.pdl.converters.DateScalarConverter"
        )
    )
    customScalars.add(
        GraphQLScalar(
            "DateTime",
            "kotlinx.datetime.LocalDateTime",
            "no.nav.api.pdl.converters.DateTimeScalarConverter"
        )
    )
    dependsOn("downloadPDLSchema")
}