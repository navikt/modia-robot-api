package no.nav.api.oppfolging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import no.nav.common.token_client.client.MachineToMachineTokenClient

class OppfolgingService(
    tokenclient: MachineToMachineTokenClient,
) {
    private val oppfolgingClient = OppfolgingClient(tokenclient)
    private val ldap = Ldap()

    @Serializable
    class Oppfolging(
        val underOppfolging: Boolean,
        val veileder: Ldap.Veileder?,
    )

    suspend fun hentOppfolging(fnr: String): Oppfolging = withContext(Dispatchers.IO) {
        val status = oppfolgingClient.hentOppfolgingStatus(fnr)
        when (status.underOppfolging) {
            null, false -> Oppfolging(underOppfolging = false, veileder = null)
            true -> Oppfolging(underOppfolging = true, veileder = hentVeileder(fnr))
        }
    }

    suspend fun hentVeileder(fnr: String): Ldap.Veileder? = withContext(Dispatchers.IO) {
        val veileder = oppfolgingClient.hentOppfolgingVeileder(fnr)
        veileder?.veilederId?.let { ldap.hentVeilederNavn(it) }
    }
}
