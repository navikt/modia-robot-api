package no.nav


interface Env {
    companion object {
        operator fun invoke(): Env = EnvImpl()
    }
}

class EnvImpl : Env
