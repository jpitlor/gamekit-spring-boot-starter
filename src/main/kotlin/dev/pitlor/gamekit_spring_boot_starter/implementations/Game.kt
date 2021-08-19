package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IPlayer
import java.util.*

open class Game(override val code: String, override var adminId: UUID) : IGame {
    override val players = arrayListOf<IPlayer>()
    override var isActive = false
}
