package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGameRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConditionalOnProperty(
    prefix = "dev.pitlor",
    name = ["persistenceStrategy"],
    havingValue = "memory",
    matchIfMissing = true
)
@ConditionalOnMissingBean(IGameRepository::class)
class MemoryGameRepository : IGameRepository {
    private val games = arrayListOf<IGame>()

    override fun findAll(filter: (IGame) -> Boolean): List<IGame> {
        return games.filter(filter)
    }

    override fun getAllByNotStarted(): List<IGame> {
        return games.filter { !it.isActive }
    }

    override fun getByCode(gameCode: String): IGame? {
        return games.find { it.code == gameCode }
    }

    override fun add(game: IGame) {
        games += game
    }

    override fun removeByCode(gameCode: String) {
        games.removeAll { it.code == gameCode }
    }
}