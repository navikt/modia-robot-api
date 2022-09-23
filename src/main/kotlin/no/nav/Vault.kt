package no.nav

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readText

object Vault {
    private val secretsPath = "/var/run/secrets/nais.io"

    class Credential(val username: String, val passord: String)

    fun readCredentials(name: String): Credential {
        val path = Paths.get(secretsPath, name)
        val username = readVaultfile(path.resolve("username"))
        val password = readVaultfile(path.resolve("password"))
        return Credential(username, password)
    }

    fun readVaultfile(file: Path): String = file.readText()
    fun fromVaultfile(file: Path, block: (String) -> Unit) = block(readVaultfile(file))
}
