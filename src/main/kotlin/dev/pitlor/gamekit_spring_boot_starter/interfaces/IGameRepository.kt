package dev.pitlor.gamekit_spring_boot_starter.interfaces

interface IGameRepository<P : IPlayer, G : IGame<P>> {
    fun findAll(filter: (G) -> Boolean): List<G>
    fun getAllByNotStarted(): List<G>
    fun getByCode(gameCode: String): G?
    fun add(game: G)
    fun removeByCode(gameCode: String)
}