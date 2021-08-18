package dev.pitlor.gamekit_spring_boot_starter.implementations

import dev.pitlor.gamekit_spring_boot_starter.interfaces.IGameList
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "dev.pitlor",
    name = ["persistenceStrategy"],
    havingValue = "memory",
    matchIfMissing = true
)
class MemoryGameList : IGameList {
}