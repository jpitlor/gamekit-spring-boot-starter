import kotlinx.coroutines.runBlocking
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Controller
open class StaticFiles {
    @RequestMapping(value = ["/{path:^(?!websocket-server)[^\\\\.]*}"])
    open fun spa(@PathVariable path: String): String {
        return "forward:/"
    }
}

@Controller
open class BaseController(private val server: Server, private val socket: SimpMessagingTemplate) {
    @EventListener
    fun onConnect(e: SessionConnectEvent) {
        if (e.user == null) return

        val user = e.user as User
        server.findCodeOfGameWithPlayer(user.id)?.let {
            server.updateSettings(it, user.id, mutableMapOf(SETTING_CONNECTED to true))
            socket.convertAndSend("/topic/games/$it", server.getGame(it))
        }
    }

    @EventListener
    fun onDisconnect(e: SessionDisconnectEvent) {
        if (e.user == null) return

        val user = e.user as User
        server.findCodeOfGameWithPlayer(user.id)?.let {
            server.updateSettings(it, user.id, mutableMapOf(SETTING_CONNECTED to false))
            socket.convertAndSend("/topic/games/$it", server.getGame(it))
        }
    }

    @MessageExceptionHandler
    @SendToUser("/topic/errors/client")
    fun on400Error(e: IllegalArgumentException): String {
        return e.message ?: ""
    }

    @MessageExceptionHandler
    @SendToUser("/topic/errors/server")
    fun on500Error(e: IllegalStateException): String {
        return e.message ?: ""
    }

    @SubscribeMapping("/rejoin-game")
    fun findLastGame(@ModelAttribute user: User): String? {
        return server.findCodeOfGameWithPlayer(user.id)
    }

    @SubscribeMapping("/games")
    fun getGames(): Iterable<String> {
        return server.getGameCodes()
    }

    @SubscribeMapping("/games/{gameCode}")
    fun getGame(@DestinationVariable gameCode: String): Game {
        return server.getGame(gameCode)
    }

    @MessageMapping("/games/{gameCode}/create")
    @SendToUser("/topic/successes")
    fun createGame(@DestinationVariable gameCode: String, @ModelAttribute user: User): String {
        val response = server.createGame(gameCode, user.id)
        socket.convertAndSend("/topic/games", server.getGameCodes())
        return response
    }

    @MessageMapping("/games/{gameCode}/join")
    @SendTo("/topic/games/{gameCode}")
    fun joinGame(
        @DestinationVariable gameCode: String,
        @Payload settings: MutableMap<String, Any>,
        @ModelAttribute user: User
    ): Game {
        server.joinGame(gameCode, user.id, settings)
        return server.getGame(gameCode)
    }

    @MessageMapping("/games/{gameCode}/update")
    @SendTo("/topic/games/{gameCode}")
    fun updateSettings(
        @DestinationVariable gameCode: String,
        @Payload settings: MutableMap<String, Any>,
        @ModelAttribute user: User
    ): Game {
        server.updateSettings(gameCode, user.id, settings)
        return server.getGame(gameCode)
    }

    @MessageMapping("/games/{gameCode}/become-admin")
    @SendToUser("/topic/successes")
    fun becomeAdmin(@DestinationVariable gameCode: String, @ModelAttribute user: User): String = runBlocking {
        val response = server.becomeAdmin(gameCode, user.id)
        socket.convertAndSend("/topic/games/$gameCode", server.getGame(gameCode))
        return@runBlocking response
    }
}