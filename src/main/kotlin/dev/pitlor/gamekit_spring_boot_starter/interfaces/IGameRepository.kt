package dev.pitlor.gamekit_spring_boot_starter.interfaces

interface IGameRepository {
    fun findAll(filter: (IGame) -> Boolean): List<IGame>
    fun getAllByNotStarted(): List<IGame>
    fun getByCode(gameCode: String): IGame?
    fun add(game: IGame)
    fun removeByCode(gameCode: String)
}