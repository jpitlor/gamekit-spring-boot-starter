import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import java.util.*

@ConfigurationProperties(prefix = "dev.pitlor.gamekit")
open class GameKitProperties {
    /**
     * Endpoint that your client will use to connect to the websocket server
     */
    var wsEndpoint: String = "/websocket-server"

    /**
     * Prefix on subscribable routes
     */
    var subscriptionPrefix: String = "/topic"

    /**
     * Prefix on non-subscribable routes
     */
    var routePrefix: String = "/app"
}

@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(GameKitProperties::class)
open class SocketConfig(private val properties: GameKitProperties) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker(properties.subscriptionPrefix)
        registry.setApplicationDestinationPrefixes(properties.routePrefix, properties.subscriptionPrefix)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint(properties.wsEndpoint).setAllowedOriginPatterns("*").withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
                if (accessor.command == StompCommand.CONNECT) {
                    val uuid = accessor.getNativeHeader("uuid")?.get(0)
                    accessor.user = User(UUID.fromString(uuid))
                }

                return message
            }
        })
    }
}

const val SETTING_CONNECTED = "connected"