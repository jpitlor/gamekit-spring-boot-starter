package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IServer
import java.util.*

open class Server : IServer {
    override fun findCodeOfGameWithPlayer(id: UUID): String? {
        TODO("Not yet implemented")
    }

    override fun updateSettings(gameCode: String, userId: UUID, newSettings: MutableMap<String, Any>) {
        TODO("Not yet implemented")
    }

    override fun getGame(gameCode: String): IGame {
        TODO("Not yet implemented")
    }

    override fun getGameCodes(): List<String> {
        TODO("Not yet implemented")
    }

    override fun createGame(gameCode: String, adminUserId: UUID): String {
        TODO("Not yet implemented")
    }

    override fun joinGame(gameCode: String, userId: UUID, settings: MutableMap<String, Any>) {
        TODO("Not yet implemented")
    }

    override suspend fun becomeAdmin(gameCode: String, userId: UUID): String {
        TODO("Not yet implemented")
    }
}