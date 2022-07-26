package no.nav.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T> externalServiceCall(noinline call: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, call)