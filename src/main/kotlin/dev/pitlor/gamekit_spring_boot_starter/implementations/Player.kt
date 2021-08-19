package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.interfaces.IPlayer
import java.time.LocalDateTime
import java.util.*

open class Player(override val id: UUID, override var settings: MutableMap<String, Any>) : IPlayer {
    override var startOfTimeOffline: LocalDateTime? = null
}