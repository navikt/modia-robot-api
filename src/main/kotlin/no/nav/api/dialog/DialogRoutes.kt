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

fun Route.configureDialogRoutes(dialogService: DialogService) {
    route("dialog/{fnr}") {
        route("sendinfomelding") {
            install(NotarizedRoute()) {
                post = Api.sendInfoMelding
            }
            post {
                val payload = call.getJWT()
                val fnr = requireNotNull(call.parameters["fnr"])
                val request: MeldingRequest = call.receive()
                val ident = call.getJWTPrincipalSubject()
                call.respond(
                    dialogService.sendInfomelding(
                        fnr,
                        request,
                        ident,
                        payload,
                    ),
                )
            }
        }
        route("sendsporsmal") {
            install(NotarizedRoute()) {
                post = Api.sendSporsmal
            }
            post {
                val payload = call.getJWT()
                val fnr = requireNotNull(call.parameters["fnr"])
                val request: MeldingRequest = call.receive()
                val ident = call.getJWTPrincipalSubject()
                call.respond(
                    dialogService.sendSporsmal(
                        fnr,
                        request,
                        ident,
                        payload,
                    ),
                )
            }
        }
    }
}

private object Api {
    val sendInfoMelding =
        PostInfo.builder {
            summary("Sender infomelding til bruker")
            description("")
            request {
                requestType(typeOf<MeldingRequest>())
                parameters {
                    CommonModels.fnrParameter
                }
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
                requestType(typeOf<MeldingRequest>())
                parameters {
                    CommonModels.fnrParameter
                }
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
