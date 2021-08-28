package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGameRepository
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IPlayer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@ConditionalOnProperty(
    prefix = "dev.pitlor",
    name = ["persistenceStrategy"],
    havingValue = "memory",
    matchIfMissing = true
)
class MemoryGameRepository<P : IPlayer, G : IGame<P>> : IGameRepository<P, G> {
    private val games = arrayListOf<G>()

    override fun findAll(filter: (G) -> Boolean): List<G> {
        return games.filter(filter)
    }

    override fun getAllByNotStarted(): List<G> {
        return games.filter { !it.isActive }
    }

    override fun getByCode(gameCode: String): G? {
        return games.find { it.code == gameCode }
    }

    override fun add(game: G) {
        games += game
    }

    override fun removeByCode(gameCode: String) {
        games.removeAll { it.code == gameCode }
    }
}