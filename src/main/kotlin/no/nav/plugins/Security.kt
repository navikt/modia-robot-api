package no.nav.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import no.nav.Env
import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit

const val SECURITY_SCHEME_NAME = "auth-jwt"
const val NAV_IDENT_CLAIM = "NAVident"
private val mockJwt = JWT.decode(
    JWT.create().withSubject(UUID.randomUUID().toString()).withClaim(NAV_IDENT_CLAIM, "Z999999").sign(Algorithm.none()),
)

fun Application.configureSecurity(disableSecurity: Boolean, env: Env) {
    if (disableSecurity) {
        authentication {
            jwt(SECURITY_SCHEME_NAME) {
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
        jwt(SECURITY_SCHEME_NAME) {
            verifier(makeJwkProvider(env.jwksUrl))
            validate { credential ->
                val hasLowercase = credential.payload.subject.contains(Regex("[a-z]"))
                if (hasLowercase) {
                    this@configureSecurity.log.warn("Detected subject with lowercase value: ${credential.payload.subject}")
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
