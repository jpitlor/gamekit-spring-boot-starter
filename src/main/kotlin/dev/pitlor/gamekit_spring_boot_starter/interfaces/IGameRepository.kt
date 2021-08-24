package dev.pitlor.gamekit_spring_boot_starter.interfaces

import java.util.*

interface IGameRepository<P : IPlayer, G : IGame<P>> {
    fun findAll(filter: (G) -> Boolean): List<G>
    fun getAllByNotStarted(): List<G>
    fun getByCode(gameCode: String): G?
    fun add(game: G)
    fun removeByCode(gameCode: String)

    private fun checkGameExists(game: G?, andHasAdmin: UUID? = null): G {
        require(game != null) { "That game does not exist" }
        if (andHasAdmin != null) {
            require(game.adminId == andHasAdmin) { "You are not the admin of that game" }
        }

        return game
    }

    fun safeGetByCode(gameCode: String, withAdmin: UUID? = null): G {
        val game = getByCode(gameCode)
        return checkGameExists(game, withAdmin)
    }
}