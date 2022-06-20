package no.nav.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.bkbn.kompendium.auth.configuration.JwtAuthConfiguration
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*

val securityScheme = object : JwtAuthConfiguration {
    override val name: String = "jwt"
}
private val mockJwt = JWT.decode(JWT.create().withSubject("Z999999").sign(Algorithm.none()))
fun Application.configureSecurity(disableSecurity: Boolean) {
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
            val jwtAudience = environment.config.property("jwt.audience").getString()
            realm = environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256("secret"))
                    .withAudience(jwtAudience)
                    .withIssuer(environment.config.property("jwt.domain").getString())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
