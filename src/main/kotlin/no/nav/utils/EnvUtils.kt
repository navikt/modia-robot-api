package no.nav.utils

fun getRequiredProperty(name: String): String = requireNotNull(getOptionalProperty(name)) {
    "Could not find property/env for '$name'"
}
fun getOptionalProperty(name: String): String? {
    return System.getProperty(name, System.getenv(name))
}

private val prodClusters = arrayOf("prod-fss", "prod-sbs", "prod-gcp")
fun isProd(): Boolean = prodClusters.contains(getOptionalProperty("NAIS_CLUSTER_NAME"))
fun isNotProd(): Boolean = !isProd()