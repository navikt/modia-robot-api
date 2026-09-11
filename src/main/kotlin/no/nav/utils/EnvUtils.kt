package no.nav.utils

import no.nav.personoversikt.common.utils.EnvUtils

val appImage: String = EnvUtils.getConfig("NAIS_APP_IMAGE") ?: "N/A"

/**
 * Versjonen av applikasjonen, hentet fra taggen i `NAIS_APP_IMAGE`.
 *
 * Nais setter imaget til `ghcr.io/navikt/modia-robot-api:<tag>`. Taggen settes av byggeprosessen og
 * er det nærmeste vi kommer et versjonsnummer. Lokalt og i tester finnes ikke variabelen, og da
 * brukes `"lokal"`.
 */
val appVersjon: String =
    appImage
        .substringAfterLast(':', "")
        .ifBlank { "lokal" }

private val prodClusters = arrayOf("prod-fss", "prod-sbs", "prod-gcp")

fun isProd(): Boolean = prodClusters.contains(EnvUtils.getConfig("NAIS_CLUSTER_NAME"))

fun isNotProd(): Boolean = !isProd()
