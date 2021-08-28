package dev.pitlor.gamekit_spring_boot_starter.interfaces

import java.util.*
import kotlin.collections.ArrayList

interface IGame<P : IPlayer> {
    val code: String
    val players: ArrayList<P>
    var isActive: Boolean
    var adminId: UUID

    fun safeGetPlayer(id: UUID): P {
        val player = players.find { it.id == id }
        require(player != null) { "That player is not in this game" }

        return player
    }
}