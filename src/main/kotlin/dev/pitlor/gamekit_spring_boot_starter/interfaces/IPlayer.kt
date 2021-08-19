package dev.pitlor.gamekit_spring_boot_starter.interfaces

import java.time.LocalDateTime
import java.util.*

interface IPlayer {
    val id: UUID
    var settings: MutableMap<String, Any>
    var startOfTimeOffline: LocalDateTime?
}