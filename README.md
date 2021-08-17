# GameKit Spring Boot Starter

This is a Spring Boot Starter with infrastructure
for making a real time web based game. It
is meant to be paired with the npm package 
`@piticent123/gamekit-client`. 

# Usage

### Installation

To install, simply add the package to your dependencies.

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>dev.pitlor</groupId>
            <artifactId>gamekit-spring-boot-starter</artifactId>
            <version>x.y.z</version>
        </dependency>
    </dependencies>
</project>
```

You still need to use the `spring-boot-starter-parent` like
any other Spring Boot starter.

### Code

The only requirement to make this starter work is that
your code must provide a bean of type `Server`. In the future,
there will be a default implementation you can override.

```java
import dev.pitlor.gamekit_spring_boot_starter.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class MyServer implements Server {
    // provide implementations for the Server methods here
}

@Configuration
class Beans {
    private static final Server server = new MyServer();

    @Bean
    public static Server getServer() {
        return server;
    }
}
```

### Configuration

This package has a number of dependencies that it has defaults for,
but you can provide your own.

1. Jackson's `KotlinModule`