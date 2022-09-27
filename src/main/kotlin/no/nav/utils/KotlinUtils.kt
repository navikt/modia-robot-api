package no.nav.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> externalServiceCall(call: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, call)

fun <K, V> Map<K, V>.swapKeyValue(): Map<V, K> = this.entries.associate { (k, v) -> v to k }
