package lxthon.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DotenvConfig {
    
    @Bean
    @Primary
    public Dotenv dotenv() {
        return Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
    }
}