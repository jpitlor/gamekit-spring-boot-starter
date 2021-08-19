package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.SETTING_CONNECTED
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGameRepository
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IPlayer
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IServer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
@ConditionalOnMissingBean(IServer::class)
open class Server<P : IPlayer, G : IGame<P>>(
    private val gameRepository: IGameRepository<P, G>,
    private val gameFactory: (code: String, adminId: UUID) -> G,
    private val playerFactory: (UUID, MutableMap<String, Any>) -> P
) : IServer {
    private val mutex = Mutex()

    override fun findCodeOfGameWithPlayer(id: UUID): String? {
        return gameRepository
            .findAll { game -> game.players.any { it.id == id } }
            .firstOrNull()
            ?.code
    }

    override fun updateSettings(gameCode: String, userId: UUID, newSettings: MutableMap<String, Any>) {
        val game = gameRepository.getByCode(gameCode)
        require(game != null) { "That game doesn't exist"}

        val player = game.players.find { it.id == userId }
        require(player != null) { "That player doesn't exist"}

        player.settings.putAll(newSettings)
        if (newSettings[SETTING_CONNECTED] == true) {
            player.startOfTimeOffline = null
        } else if (newSettings[SETTING_CONNECTED] == false) {
            player.startOfTimeOffline = LocalDateTime.now()
        }
    }

    override fun getGame(gameCode: String): G {
        val game = gameRepository.getByCode(gameCode)

        require(game != null) { "That game doesn't exist"}

        return game
    }

    override fun getGameCodes(): List<String> {
        return gameRepository.getAllByNotStarted().map { it.code }
    }

    override fun createGame(gameCode: String, adminUserId: UUID): String {
        require(gameCode.isNotEmpty()) { "Code is empty" }
        require(gameRepository.getByCode(gameCode) == null) { "That game does not exist" }

        val game = gameFactory(gameCode, adminUserId)
        gameRepository.add(game)

        return "Game \"${gameCode}\" Created"
    }

    override fun joinGame(gameCode: String, userId: UUID, settings: MutableMap<String, Any>) {
        val game = gameRepository.getByCode(gameCode)
        require(gameCode.isNotEmpty()) { "Code is empty" }
        require(game != null) { "That game does not exist" }
        require(game.players.find { it.id == userId } == null) { "You are already in that game!" }

        settings[SETTING_CONNECTED] = true
        val player = playerFactory(userId, settings)
        game.players += player
    }

    override suspend fun becomeAdmin(gameCode: String, userId: UUID): String {
        mutex.withLock {
            val game = gameRepository.getByCode(gameCode)

            require(game != null) { "That game doesn't exist"}
            check(game.players.find { it.id == game.adminId }?.startOfTimeOffline == null) { "Someone already claimed the admin spot" }
            game.adminId = userId
        }

        return "You are now the game admin"
    }
}