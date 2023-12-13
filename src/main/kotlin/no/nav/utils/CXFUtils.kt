package no.nav.utils

@Suppress("ktlint:standard:function-naming")
inline fun <reified T> CXFClient() = no.nav.common.cxf.CXFClient(T::class.java)
