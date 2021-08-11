import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.context.annotation.Bean

@Bean
fun getJacksonKotlinModule(): KotlinModule {
    return kotlinModule()
}