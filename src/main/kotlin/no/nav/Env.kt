package no.nav


interface Env {
    companion object {
        operator fun invoke(): Env = EnvImpl()
    }
    val tpsPersonV3Url: String
}

class EnvImpl : Env {
    override val tpsPersonV3Url: String
        get() = TODO("Not yet implemented")
}
