package dev.pitlor.gamekit_spring_boot_starter

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@ConditionalOnMissingBean
@Bean
fun getJacksonKotlinModule(): KotlinModule {
    return kotlinModule()
}