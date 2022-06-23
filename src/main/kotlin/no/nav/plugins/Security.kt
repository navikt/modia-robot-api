package no.nav.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.bkbn.kompendium.auth.configuration.JwtAuthConfiguration
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import no.nav.utils.getRequiredProperty
import java.net.URL
import java.util.concurrent.TimeUnit

val securityScheme = object : JwtAuthConfiguration {
    override val name: String = "jwt"
}
private val mockJwt = JWT.decode(JWT.create().withSubject("Z999999").sign(Algorithm.none()))
fun Application.configureSecurity(disableSecurity: Boolean, jwksUrl: String) {
    val allowList = getRequiredProperty("IDENT_ALLOW_LIST").split(",")
    if (disableSecurity) {
        authentication {
            jwt(securityScheme.name) {
                verifier {
                    object : JWTVerifier {
                        override fun verify(token: String?): DecodedJWT = mockJwt
                        override fun verify(jwt: DecodedJWT?): DecodedJWT = mockJwt
                    }
                }
                validate { object : Principal {} }
                realm = "modia-robot-api"
            }
        }
        return
    }

    authentication {
        jwt(securityScheme.name) {
            verifier(makeJwkProvider(jwksUrl))
            validate { credential ->
                when {
                    credential.payload.audience == null -> null
                    allowList.contains(credential.payload.subject) -> JWTPrincipal(credential.payload)
                    else -> null
                }
            }
        }
    }
}

private fun makeJwkProvider(jwksUrl: String): JwkProvider =
    JwkProviderBuilder(URL(jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()