package no.nav.api.oppfolging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import no.nav.utils.getRequiredProperty
import java.util.Hashtable
import javax.naming.Context
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

class Ldap {
    val ldapUrl = getRequiredProperty("LDAP_URL")
    val ldapUsername = getRequiredProperty("LDAP_USERNAME")
    val ldapPassword = getRequiredProperty("LDAP_PASSWORD")
    val ldapBasedn = getRequiredProperty("LDAP_BASEDN")

    @Serializable
    class Veileder(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    )

    private val searchBase = "OU=Users,OU=NAV,OU=BusinessUnits,$ldapBasedn"

    suspend fun hentVeilederNavn(ident: String): Veileder? = withContext(Dispatchers.IO) {
        search(ident)
            .firstOrNull()
            ?.let {
                Veileder(
                    fornavn = it.attributes.get("givenname").get() as String,
                    etternavn = it.attributes.get("sn").get() as String,
                    ident = ident
                )
            }
    }

    private suspend fun search(ident: String): Sequence<SearchResult> = withContext(Dispatchers.IO) {
        val searchCtrl = SearchControls().apply {
            searchScope = SearchControls.SUBTREE_SCOPE
        }
        getLdapContext()
            .search(
                searchBase,
                "(&(objectClass=user)(CN=$ident))",
                searchCtrl
            ).asSequence()
    }

    private val ldapEnvironment = Hashtable(
        mutableMapOf(
            Context.INITIAL_CONTEXT_FACTORY to "com.sun.jndi.ldap.LdapCtxFactory",
            Context.SECURITY_AUTHENTICATION to "simple",
            Context.PROVIDER_URL to ldapUrl,
            Context.SECURITY_PRINCIPAL to ldapUsername,
            Context.SECURITY_CREDENTIALS to ldapPassword,
        )
    )

    private fun getLdapContext(): LdapContext = InitialLdapContext(ldapEnvironment, null)
}
