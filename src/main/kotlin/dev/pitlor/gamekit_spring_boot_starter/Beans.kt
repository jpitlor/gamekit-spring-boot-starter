package dev.pitlor.gamekit_spring_boot_starter

import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.pitlor.gamekit_spring_boot_starter.implementations.Game
import dev.pitlor.gamekit_spring_boot_starter.implementations.Player
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGame
import dev.pitlor.gamekit_spring_boot_starter.interfaces.IPlayer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
open class Beans {
    @ConditionalOnMissingBean
    @Bean
    open fun getKotlinModule(): KotlinModule {
        return KotlinModule.Builder().build()
    }

    @ConditionalOnMissingBean
    @Bean
    open fun gameFactory(): (code: String, adminId: UUID) -> IGame<IPlayer> {
        return ::Game
    }

    @ConditionalOnMissingBean
    @Bean
    open fun playerFactory(): (UUID, MutableMap<String, Any>) -> Player {
        return ::Player
    }
}