package lxthon.backend.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Global CORS configuration for the application.
 * <p>
 * Defines a {@link CorsFilter} bean that allows cross-origin requests
 * from the frontend (e.g., http://localhost:3000) and permits all HTTP methods
 * and headers. Credentials (cookies, authorization headers) are also allowed.
 * </p>
 */
@Configuration
public class CorsConfig {

    /**
     * Creates and registers a {@link CorsFilter} that applies to all routes (/**).
     * <p>
     * This filter will:
     * <ul>
     *   <li>Allow requests from <code>http://localhost:3000</code>.</li>
     *   <li>Allow all HTTP methods (GET, POST, PUT, DELETE, etc.).</li>
     *   <li>Allow all headers.</li>
     *   <li>Allow credentials (cookies and authorization headers).</li>
     * </ul>
     * </p>
     *
     * @return a {@link CorsFilter} configured with the above settings
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000"); 
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
