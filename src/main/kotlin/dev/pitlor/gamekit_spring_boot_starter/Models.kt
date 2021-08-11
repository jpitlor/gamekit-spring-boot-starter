package dev.pitlor.gamekit_spring_boot_starter

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import java.security.Principal
import java.util.*

data class User(val id: UUID) : Principal {
    override fun getName(): String {
        return id.toString()
    }
}

@ControllerAdvice
class CustomPrincipal {
    @ModelAttribute
    fun getPrincipal(principal: Principal?): User? {
        if (principal == null) return null
        return principal as User
    }
}

interface Server {
    fun findCodeOfGameWithPlayer(id: UUID): String?
    fun updateSettings(gameCode: String, userId: UUID, newSettings: Map<String, Any>)
    fun getGame(gameCode: String): Game
    fun getGameCodes(): List<String>
    fun createGame(gameCode: String, adminUserId: UUID): String
    fun joinGame(gameCode: String, userId: UUID, settings: Map<String, Any>)
    fun becomeAdmin(gameCode: String, userId: UUID): String
}

interface Game {

}

interface Player {

}