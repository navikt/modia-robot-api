package no.nav.api

import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import kotlinx.serialization.Serializable

object CommonModels {
    @Serializable
    class FnrParameter(
        @Param(type = ParamType.PATH)
        val fnr: String,
    )
}
