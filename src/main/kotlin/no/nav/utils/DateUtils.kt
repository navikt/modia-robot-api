package no.nav.utils

import kotlinx.datetime.*
import org.joda.time.DateTime
import kotlin.time.Duration
import org.joda.time.LocalDate as JodaLocalDate

fun LocalDateTime.Companion.now(clock: Clock = Clock.System): LocalDateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
fun LocalDate.Companion.now(clock: Clock = Clock.System): LocalDate = LocalDateTime.now(clock).date

fun LocalDateTime.minus(duration: Duration): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    val instant = this.toInstant(timeZone)
    return instant.minus(duration).toLocalDateTime(timeZone)
}
fun LocalDateTime.minus(value: Int, unit: DateTimeUnit.TimeBased): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    val instant = this.toInstant(timeZone)
    return instant.minus(value, unit).toLocalDateTime(timeZone)
}

fun LocalDateTime.plus(duration: Duration): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    val instant = this.toInstant(timeZone)
    return instant.plus(duration).toLocalDateTime(timeZone)
}

fun LocalDate.toJodaLocalDate(): JodaLocalDate {
    return JodaLocalDate(this.toJavaLocalDate())
}

fun DateTime.toKotlinLocalDate(): LocalDate {
    return LocalDate(this.year, this.monthOfYear, this.dayOfMonth)
}