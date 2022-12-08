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
import io.ktor.http.*
import io.ktor.response.*
import no.nav.Env
import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit

val securityScheme = object : JwtAuthConfiguration {
    override val name: String = "jwt"
}
const val NAV_IDENT_CLAIM = "NAVident"
private val mockJwt = JWT.decode(
    JWT.create().withSubject(UUID.randomUUID().toString()).withClaim(NAV_IDENT_CLAIM, "Z999999").sign(Algorithm.none())
)

fun Application.configureSecurity(disableSecurity: Boolean, env: Env) {
    if (disableSecurity) {
        authentication {
            jwt(securityScheme.name) {
                verifier {
                    object : JWTVerifier {
                        override fun verify(token: String?): DecodedJWT = mockJwt
                        override fun verify(jwt: DecodedJWT?): DecodedJWT = mockJwt
                    }
                }
                validate { JWTPrincipal(mockJwt) }
                realm = "modia-robot-api"
            }
        }
        return
    }

    authentication {
        jwt(securityScheme.name) {
            verifier(makeJwkProvider(env.jwksUrl))
            validate { credential ->
                val hasLowercase = credential.payload.subject.contains(Regex("[a-z]"))
                if (hasLowercase) {
                    log.warn("Detected subject with lowercase value: ${credential.payload.subject}")
                }
                when {
                    credential.payload.audience == null -> null
                    env.identAllowList.contains(credential.getSubject()) -> JWTPrincipal(credential.payload)
                    else -> null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

private fun JWTPayloadHolder.getSubject(): String =
    payload.claims[NAV_IDENT_CLAIM]?.asString() ?: payload.subject.uppercase()

private fun makeJwkProvider(jwksUrl: String): JwkProvider =
    JwkProviderBuilder(URL(jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
