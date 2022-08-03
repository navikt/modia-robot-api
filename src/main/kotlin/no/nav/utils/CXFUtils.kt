package no.nav.utils

inline fun <reified T> CXFClient() = no.nav.common.cxf.CXFClient(T::class.java)
