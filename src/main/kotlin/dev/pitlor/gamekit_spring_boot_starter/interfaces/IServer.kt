package dev.pitlor.gamekit_spring_boot_starter.interfaces

import java.util.*

interface IServer<P : IPlayer, G : IGame<P>> {
    fun findCodeOfGameWithPlayer(id: UUID): String?
    fun updateSettings(gameCode: String, userId: UUID, newSettings: MutableMap<String, Any>)
    fun getGame(gameCode: String): G
    fun getGameCodes(): List<String>
    fun createGame(gameCode: String, adminUserId: UUID): String
    fun joinGame(gameCode: String, userId: UUID, settings: MutableMap<String, Any>)
    suspend fun becomeAdmin(gameCode: String, userId: UUID): String
}