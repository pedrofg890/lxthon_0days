package lxthon.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Spring configuration class for loading environment variables from a `.env` file.
 * <p>
 * Defines a primary {@link Dotenv} bean that can be injected throughout the application
 * for accessing environment variables. It is configured to ignore the absence of a `.env` file,
 * allowing the application to run even if no file is present.
 * </p>
 */
@Configuration
public class DotenvConfig {

    /**
     * Creates and configures a {@link Dotenv} instance as the primary bean.
     * <p>
     * This Dotenv instance will:
     * <ul>
     *   <li>Load environment variables from a file named <code>.env</code> in the working directory.</li>
     *   <li>Ignore if the file is missing, allowing fallback to system environment variables.</li>
     * </ul>
     * </p>
     *
     * @return a configured {@link Dotenv} instance for injecting environment variables
     */
    @Bean
    @Primary
    public Dotenv dotenv() {
        return Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
    }
}