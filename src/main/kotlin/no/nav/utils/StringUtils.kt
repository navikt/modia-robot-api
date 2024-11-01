package no.nav.utils

private val mask = Regex("\\d{6,}")

fun String.masked(): String =
    this.replace(mask) {
        it.value.substring(0, 6) + "*".repeat(it.value.length - 6)
    }
