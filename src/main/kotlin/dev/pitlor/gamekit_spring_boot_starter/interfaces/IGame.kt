package dev.pitlor.gamekit_spring_boot_starter.interfaces

import java.util.*
import kotlin.collections.ArrayList

interface IGame {
    val code: String
    val players: ArrayList<IPlayer>
    var isActive: Boolean
    var adminId: UUID
}