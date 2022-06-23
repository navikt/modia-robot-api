package no.nav.api.digdir

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.metadata.ExceptionInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.ktor.http.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels
import no.nav.api.IsoLocalDateSerializer
import no.nav.plugins.securityScheme
import java.time.LocalDate
import kotlin.reflect.typeOf

fun Route.configureDigdirRoutes() {
    route("digdir/{fnr}/epost") {
        notarizedGet(Api.epost) {
            TODO()
        }
    }
}
private object Api {
    val epost = GetInfo<CommonModels.FnrParameter, Models.Epost>(
        summary = "Brukers epost",
        description = "Hentes fra digdir-proxy",
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Brukers epost"
        ),
        tags = setOf("Kontakt og reservasjons registeret"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}

private object Models {
    @Serializable
    data class Epost(
        val value: String,
        @Serializable(with = IsoLocalDateSerializer::class)
        val sistOppdatert: LocalDate?,
        @Serializable(with = IsoLocalDateSerializer::class)
        val sistVerifisert: LocalDate?,
    )
}
