package no.nav.api.dialog

import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo
import io.ktor.http.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.nav.api.CommonModels

fun Route.configureDialogRoutes() {
    route("dialog/{fnr}") {
        route("sendinfomelding") {
            notarizedPost(Api.sendInfomelding) {
                TODO()
            }
        }
        route("sendsporsmal") {
            notarizedPost(Api.sendSporsmal) {
                TODO()
            }
        }
    }
}

private object Api {
    val sendInfomelding = PostInfo<CommonModels.FnrParameter, Models.SendInfomeldingRequest, Models.Response>(
        summary = "Sender infomelding til bruker",
        description = "",
        requestInfo = RequestInfo(
            description = "Innholdet i meldingen, og temaet meldingen skal knyttes"
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Referanse til meldingen som ble sendt til bruker"
        ),
        tags = setOf("Dialog"),
        canThrow = CommonModels.standardResponses,
    )

    val sendSporsmal = PostInfo<CommonModels.FnrParameter, Models.SendSporsmalRequest, Models.Response>(
        summary = "Sender infomelding til bruker",
        description = "",
        requestInfo = RequestInfo(
            description = "Innholdet i meldingen, og temaet meldingen skal knyttes"
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Referanse til meldingen som ble sendt til bruker"
        ),
        tags = setOf("Dialog"),
        canThrow = CommonModels.standardResponses,
    )
}
private object Models {
    @Serializable
    data class SendInfomeldingRequest(
        val tekst: String,
        val tema: String,
    )

    @Serializable
    data class SendSporsmalRequest(
        val tekst: String,
        val tema: String,
        val svarSkalTilEnhetsOppgavebenk: Boolean = true,
    )

    @Serializable
    data class Response(
        val kjedeId: String,
    )
}
