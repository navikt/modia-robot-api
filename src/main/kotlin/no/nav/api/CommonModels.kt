package no.nav.api

import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import io.bkbn.kompendium.core.metadata.ExceptionInfo
import io.ktor.http.*
import kotlin.reflect.typeOf

object CommonModels {
    open class FnrParameter(
        @Param(type = ParamType.PATH)
        val fnr: String,
    )

    val noContent = ExceptionInfo<Unit>(
        responseType = typeOf<Unit>(),
        status = HttpStatusCode.NoContent,
        description = "Ingen informasjon funnet"
    )
    val internalServerError = ExceptionInfo<Unit>(
        responseType = typeOf<Unit>(),
        status = HttpStatusCode.InternalServerError,
        description = "Det skjedde en feil ved henting av data"
    )

    val standardResponses = setOf(
        noContent,
        internalServerError
    )
}
