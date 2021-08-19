package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IPlayer
import java.util.*

open class Game<P : IPlayer>(override val code: String, override var adminId: UUID) : IGame<P> {
    override val players = arrayListOf<P>()
    override var isActive = false
}
