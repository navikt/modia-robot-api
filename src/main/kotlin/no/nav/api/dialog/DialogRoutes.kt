package no.nav.api.dialog

import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.api.CommonModels
import no.nav.api.dialog.DialogService.*
import no.nav.plugins.securityScheme

fun Route.configureDialogRoutes(
    dialogService: DialogService
) {
    route("dialog/{fnr}") {
        route("sendinfomelding") {
            notarizedPost(Api.sendInfomelding) {
                val fnr = requireNotNull(call.parameters["fnr"])
                val request : MeldingRequest = call.receive()
                call.respond(
                    dialogService.sendInfomelding(
                        fnr,
                        request
                    )
                )
            }
        }
        route("sendsporsmal") {
            notarizedPost(Api.sendSporsmal) {
                val fnr = requireNotNull(call.parameters["fnr"])
                val request: MeldingRequest = call.receive()
                call.respond(
                    dialogService.sendSporsmal(
                        fnr,
                        request
                    )
                )
            }
        }
    }
}

private object Api {
    val sendInfomelding = PostInfo<CommonModels.FnrParameter, MeldingRequest, Response>(
        summary = "Sender infomelding til bruker",
        description = "",
        requestInfo = RequestInfo(
            description =
                """Innholdet i meldingen, temaet meldingen skal knyttes til, og enheten som sender meldingen.
                   Tekster som inneholder referanser til brukers navn og fødselsnummer (og kun disse) vil bli omgjort med riktig verdier
                   før innsending. Eksempel på referanse: [bruker.fornavn], [bruker.etternavn] etc.
                """.trimIndent()
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Referanse til meldingen som ble sendt til bruker"
        ),
        tags = setOf("Dialog"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )

    val sendSporsmal = PostInfo<CommonModels.FnrParameter, MeldingRequest, Response>(
        summary = "Sender spørsmål til bruker",
        description = "",
        requestInfo = RequestInfo(
            description =
                """Innholdet i meldingen, temaet meldingen skal knyttes til, og enheten som sender meldingen.
                   Tekster som inneholder referanser til brukers navn og fødselsnummer (og kun disse) vil bli omgjort med riktig verdier
                   før innsending. Eksempel på referanse: [bruker.fornavn], [bruker.etternavn] etc.
                """.trimIndent()
        ),
        responseInfo = ResponseInfo(
            status = HttpStatusCode.OK,
            description = "Referanse til meldingen som ble sendt til bruker"
        ),
        tags = setOf("Dialog"),
        securitySchemes = setOf(securityScheme.name),
        canThrow = CommonModels.standardResponses,
    )
}