package no.nav.utils

import no.nav.personoversikt.utils.EnvUtils

val appImage: String = EnvUtils.getConfig("NAIS_APP_IMAGE") ?: "N/A"

private val prodClusters = arrayOf("prod-fss", "prod-sbs", "prod-gcp")
fun isProd(): Boolean = prodClusters.contains(EnvUtils.getConfig("NAIS_CLUSTER_NAME"))
fun isNotProd(): Boolean = !isProd()
