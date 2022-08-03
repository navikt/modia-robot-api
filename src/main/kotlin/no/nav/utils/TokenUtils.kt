package no.nav.utils

import no.nav.common.token_client.client.MachineToMachineTokenClient

class DownstreamApi(
    val cluster: String,
    val namespace: String,
    val application: String,
)
private fun DownstreamApi.tokenscope(): String = "api://$cluster.$namespace.$application/.default"

fun MachineToMachineTokenClient.createMachineToMachineToken(api: DownstreamApi): String {
    return this.createMachineToMachineToken(api.tokenscope())
}
