package dev.pitlor.gamekit_spring_boot_starter.implementations

import java.security.Principal
import java.util.*

data class User(val id: UUID) : Principal {
    override fun getName(): String {
        return id.toString()
    }
}