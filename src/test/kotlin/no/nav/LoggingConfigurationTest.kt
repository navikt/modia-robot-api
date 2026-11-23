package no.nav

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.filter.EvaluatorFilter
import ch.qos.logback.core.status.Status
import ch.qos.logback.core.spi.FilterReply
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoggingConfigurationTest {
    @Test
    fun `production config filters CXF non-errors from stdout`() {
        val context = LoggerContext()

        try {
            val configuration = requireNotNull(javaClass.classLoader.getResourceAsStream("logback.xml"))
            configuration.use {
                JoranConfigurator().apply { setContext(context) }.doConfigure(it)
            }

            val configurationErrors = context.statusManager.getCopyOfStatusList()
                .filter { it.level >= Status.ERROR }
            assertTrue(
                configurationErrors.isEmpty(),
                "Production logging configuration errors: ${configurationErrors.joinToString { it.message }}",
            )

            val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)
            val stdoutAppender = requireNotNull(rootLogger.getAppender("stdout_json_masked"))
            val cxfFilter = requireNotNull(
                stdoutAppender.copyOfAttachedFiltersList.filterIsInstance<EvaluatorFilter<ILoggingEvent>>()
                    .singleOrNull(),
            )
            assertEquals(
                "no.nav.common.log.CxfNonErrorEventEvaluator",
                cxfFilter.evaluator.javaClass.name,
            )

            assertEquals(
                FilterReply.DENY,
                stdoutAppender.getFilterChainDecision(
                    loggingEvent(context, "org.apache.cxf.synthetic", Level.INFO, "synthetic-cxf-info"),
                ),
            )
            assertEquals(
                FilterReply.NEUTRAL,
                stdoutAppender.getFilterChainDecision(
                    loggingEvent(context, "org.apache.cxf.synthetic", Level.ERROR, "synthetic-cxf-error"),
                ),
            )
            assertEquals(
                FilterReply.NEUTRAL,
                stdoutAppender.getFilterChainDecision(
                    loggingEvent(context, "no.nav.synthetic", Level.INFO, "synthetic-regular-info"),
                ),
            )
        } finally {
            context.stop()
        }
    }

    private fun loggingEvent(
        context: LoggerContext,
        loggerName: String,
        level: Level,
        message: String,
    ): ILoggingEvent =
        LoggingEvent(
            Logger::class.java.name,
            context.getLogger(loggerName),
            level,
            message,
            null,
            null,
        )
}
