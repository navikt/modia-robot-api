package no.nav.utils
import no.nav.personoversikt.common.logging.Logging

object TjenestekallLogger {
    private val tjenestekallLogg = Logging.teamLog

    fun info(
        header: String,
        fields: Map<String, Any?>,
    ) = tjenestekallLogg.info(Logging.TEAM_LOGS_MARKER, format(header, fields))

    fun warn(
        header: String,
        fields: Map<String, Any?>,
    ) = tjenestekallLogg.warn(Logging.TEAM_LOGS_MARKER, format(header, fields))

    fun error(
        header: String,
        fields: Map<String, Any?>,
    ) = tjenestekallLogg.error(Logging.TEAM_LOGS_MARKER, format(header, fields))

    fun error(
        header: String,
        fields: Map<String, Any?>,
        throwable: Throwable,
    ) = tjenestekallLogg.error(Logging.TEAM_LOGS_MARKER, format(header, fields), throwable)

    fun format(
        header: String,
        fields: Map<String, Any?>,
    ): String {
        val sb = StringBuilder()
        sb.appendLine(header)
        sb.appendLine("------------------------------------------------------------------------------------")
        fields.forEach { (key, value) ->
            sb.appendLine("$key: $value")
        }
        sb.appendLine("------------------------------------------------------------------------------------")
        return sb.toString()
    }

    fun format(
        header: String,
        body: String,
    ): String {
        val sb = StringBuilder()
        sb.appendLine(header)
        sb.appendLine("------------------------------------------------------------------------------------")
        sb.appendLine(body)
        sb.appendLine("------------------------------------------------------------------------------------")
        return sb.toString()
    }
}
