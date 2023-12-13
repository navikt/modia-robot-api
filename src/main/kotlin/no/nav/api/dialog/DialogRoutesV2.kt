package no.nav.api.dialog

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.api.CommonModels
import no.nav.api.dialog.DialogService.*
import no.nav.utils.getJWT
import no.nav.utils.getJWTPrincipalSubject
import kotlin.reflect.typeOf

fun Route.configureDialogRoutesV2(dialogService: DialogService) {
    route("v2/dialog") {
        route("sendinfomelding") {
            install(NotarizedRoute()) {
                post = ApiV2.sendInfoMelding
            }
            post {
                val payload = call.getJWT()
                val request: MeldingRequestV2 = call.receive()
                val ident = call.getJWTPrincipalSubject()
                call.respond(
                    dialogService.sendInfomelding(
                        request.fnr,
                        request,
                        ident,
                        payload,
                    ),
                )
            }
        }
        route("sendsporsmal") {
            install(NotarizedRoute()) {
                post = ApiV2.sendSporsmal
            }
            post {
                val payload = call.getJWT()
                val request: MeldingRequestV2 = call.receive()
                val ident = call.getJWTPrincipalSubject()
                call.respond(
                    dialogService.sendSporsmal(
                        request.fnr,
                        request,
                        ident,
                        payload,
                    ),
                )
            }
        }
    }
}

private object ApiV2 {
    val sendInfoMelding =
        PostInfo.builder {
            summary("Sender infomelding til bruker")
            description("")
            request {
                requestType(typeOf<MeldingRequestV2>())
                description(
                    """
                    Innholdet i meldingen, temaet meldingen skal knyttes til, og enheten som sender meldingen.
                    Tekster som inneholder referanser til brukers navn og fødselsnummer (og kun disse) vil bli omgjort med riktig verdier
                    før innsending. Eksempel på referanse: [bruker.fornavn], [bruker.etternavn] etc.
                    """.trimIndent(),
                )
            }
            response {
                responseType(typeOf<Response>())
                responseCode(HttpStatusCode.OK)
                description("Referanse til meldingen som ble sendt til bruker")
            }
            tags("Dialog")
            canRespond(CommonModels.standardResponses)
        }

    val sendSporsmal =
        PostInfo.builder {
            summary("Sender spørsmål til bruker")
            description("")
            request {
                requestType(typeOf<MeldingRequestV2>())
                description(
                    """
                    Innholdet i meldingen, temaet meldingen skal knyttes til, og enheten som sender meldingen.
                    Tekster som inneholder referanser til brukers navn og fødselsnummer (og kun disse) vil bli omgjort med riktig verdier
                    før innsending. Eksempel på referanse: [bruker.fornavn], [bruker.etternavn] etc.
                    """.trimIndent(),
                )
            }
            response {
                responseType(typeOf<Response>())
                responseCode(HttpStatusCode.OK)
                description("Referanse til meldingen som ble sendt til bruker")
            }
            tags("Dialog")
            canRespond(CommonModels.standardResponses)
        }
}
