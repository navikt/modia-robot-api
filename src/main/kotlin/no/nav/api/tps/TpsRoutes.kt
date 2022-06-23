package no.nav.api.tps

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.http.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels

fun Route.configureTpsRoutes() {
    route("tps/{fnr}/kontonummer") {
        notarizedGet(Api.kontonummer) {
            TODO()
        }
    }
}

private object Api {
    val kontonummer = GetInfo<CommonModels.FnrParameter, Models.Kontonummer>(
        summary = "Brukers kontonummer",
        description = "Hentes fra TPS",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers kontonummer om det eksisterer i TPS"
        ),
        tags = setOf("TPS")
    )
}

private object Models {
    @Serializable
    data class Kontonummer(
        val value: String?
    )
}
