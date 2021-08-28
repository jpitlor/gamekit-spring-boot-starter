package dev.pitlor.gamekit_spring_boot_starter

import dev.pitlor.gamekit_spring_boot_starter.implementations.User
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IPlayer
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IServer
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import java.security.Principal

/**
 * This is a class that can do conversions on parameters
 * that get autowired into controllers.
 */
@ControllerAdvice
class CustomPrincipal {
    /**
     * This Model Attribute casts the Principal that Spring autowires
     * into our custom User class - the properties are the same, but the ID is
     * a UUID instead of a string
     */
    @ModelAttribute
    fun getPrincipal(principal: Principal?): User? {
        if (principal == null) return null
        return principal as User
    }
}

/**
 * This is a controller that is concerned with static files. It technically
 * doesn't need to exist - you can have dynamic and static routes in the same
 * controller. However, I chose to separate it to make things easier to read
 */
@Controller
open class StaticFiles {
    /**
     * This starter assumes the consuming application is using Spring to run a SPA
     * as the client, so it redirects everything to "/", or `index.html`
     */
    @RequestMapping(value = ["/{path:^(?!websocket-server)[^\\\\.]*}"])
    open fun spa(@PathVariable path: String): String {
        return "forward:/"
    }
}

/**
 * This controller contains the websocket routes/event handlers.
 *
 * An `IServer` bean is required for this controller to be discovered. This requirement is enforced via a
 * ConditionalOnBean annotation for the sake of failing gracefully, but this starter provides little to no
 * benefit without this controller.
 */
@Controller
class BaseController<P : IPlayer, G : IGame<P>>(
    private val server: IServer<P, G>,
    private val socket: SimpMessagingTemplate
) {
    /**
     * We listen for "onConnect" events to update the "connected" setting on the user, which (if implemented) updates
     * indicators in the client, and, if this user is the admin of a game, lets the client know if/when users can claim
     * the admin spot
     */
    @EventListener
    fun onConnect(e: SessionConnectEvent) {
        if (e.user == null) return

        val user = e.user as User
        server.findCodeOfGameWithPlayer(user.id)?.let {
            server.updateSettings(it, user.id, mutableMapOf(SETTING_CONNECTED to true))
            socket.convertAndSend("/topic/games/$it", server.getGame(it))
        }
    }

    /**
     * We listen for "onDisconnect" events to update the "connected" setting on the user, which (if implemented) updates
     * indicators in the client, and, if this user is the admin of a game, lets the client know if/when users can claim
     * the admin spot
     */
    @EventListener
    fun onDisconnect(e: SessionDisconnectEvent) {
        if (e.user == null) return

        val user = e.user as User
        server.findCodeOfGameWithPlayer(user.id)?.let {
            server.updateSettings(it, user.id, mutableMapOf(SETTING_CONNECTED to false))
            socket.convertAndSend("/topic/games/$it", server.getGame(it))
        }
    }

    /**
     * There are various `require` calls in the application. They should pass, as the server isn't designed to be
     * connected to directly via a custom HTTP request, but (a) anything can happen and (b) sometimes consuming
     * applications prefer leaning on `require` to simplify the UI, so instead of failing, we want to catch every
     * IllegalArgumentException and send back the message to the user. These errors can be ignored or subscribed to
     * in the frontend.
     */
    @MessageExceptionHandler
    @SendToUser("/topic/errors/client")
    fun on400Error(e: IllegalArgumentException): String {
        return e.message ?: ""
    }

    /**
     * There are various `check` calls in the application. They should pass, but sometimes bugs happen and the server
     * gets into a bad state. Instead of failing, we want to catch every IllegalStateException and send back the message
     * to the user. These errors can be ignored or subscribed to in the frontend.
     */
    @MessageExceptionHandler
    @SendToUser("/topic/errors/server")
    fun on500Error(e: IllegalStateException): String {
        return e.message ?: ""
    }

    /**
     * This is not a true subscription route. It behaves similar to a REST endpoint. There is no equivalent in web
     * sockets, but it is relatively easy to implement using the stomp JS library.
     *
     * This "route" is used on page load as follows:
     * 1. The user is in the middle of a game and is disconnected for some reason
     * 2. The user reconnects and the page loads, and this route is subscribed to
     * 3. The page will get a near instant response with the code of the game the user was dropped from
     * 4. The application can automatically re-subscribe to the game, putting them right back into the action.
     */
    @SubscribeMapping("/rejoin-game")
    fun findLastGame(@ModelAttribute user: User): String? {
        return server.findCodeOfGameWithPlayer(user.id)
    }

    /**
     * Use this route to get updates whenever there is a change to the list of games that a user can join.
     *
     * When someone subscribes to the game list, we want to send them the current list of open games so the UI
     * can make an initial update.
     */
    @SubscribeMapping("/games")
    fun getGames(): Iterable<String> {
        return server.getGameCodes()
    }

    /**
     * Use this route to get updates whenever there is a change to the game with code `gameCode`
     *
     * When someone subscribes to game updates, we want to send them the current game so the UI can make an initial
     * update.
     */
    @SubscribeMapping("/games/{gameCode}")
    fun getGame(@DestinationVariable gameCode: String): G {
        return server.getGame(gameCode)
    }

    /**
     * Make a request to this route when you want to create a new game. Anyone can call this route, so it is up
     * to the UI to restrict this route to the appropriate people.
     *
     * A success message will be sent to the client if this completes successfully.
     */
    @MessageMapping("/games/{gameCode}/create")
    @SendToUser("/topic/successes")
    fun createGame(@DestinationVariable gameCode: String, @ModelAttribute user: User): String {
        val response = server.createGame(gameCode, user.id)
        socket.convertAndSend("/topic/games", server.getGameCodes())
        return response
    }

    /**
     * Make a request to this route when you want to join an existing game.
     *
     * An update will be sent to anyone that already joined the game.
     */
    @MessageMapping("/games/{gameCode}/join")
    @SendTo("/topic/games/{gameCode}")
    fun joinGame(
        @DestinationVariable gameCode: String,
        @Payload settings: MutableMap<String, Any>,
        @ModelAttribute user: User
    ): G {
        server.joinGame(gameCode, user.id, settings)
        return server.getGame(gameCode)
    }

    /**
     * Use this route when a user wants to update their settings (name, photo, etc). It updates the settings and sends
     * the updated game to the rest of the users.
     */
    @MessageMapping("/games/{gameCode}/update")
    @SendTo("/topic/games/{gameCode}")
    fun updateSettings(
        @DestinationVariable gameCode: String,
        @Payload settings: MutableMap<String, Any>,
        @ModelAttribute user: User
    ): G {
        server.updateSettings(gameCode, user.id, settings)
        return server.getGame(gameCode)
    }

    /**
     * Use this route when a user wants to try and claim the admin spot. It is only valid to call when the admin
     * has been offline for a sufficient amount of time. Due to race conditions, the claim may fail, but it will
     * do so gracefully.
     *
     * A success message will be sent to the user if the claim is successful.
     */
    @MessageMapping("/games/{gameCode}/become-admin")
    @SendToUser("/topic/successes")
    fun becomeAdmin(@DestinationVariable gameCode: String, @ModelAttribute user: User): String = runBlocking {
        val response = server.becomeAdmin(gameCode, user.id)
        socket.convertAndSend("/topic/games/$gameCode", server.getGame(gameCode))
        return@runBlocking response
    }
}