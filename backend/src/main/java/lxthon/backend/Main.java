package lxthon.backend;

import lxthon.backend.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the application.
 * <p>
 * This class is responsible for:
 * <ol>
 *   <li>Loading environment variables from a <code>.env</code> file via {@link EnvLoader}.</li>
 *   <li>Bootstrapping the Spring Boot context.</li>
 *   <li>Enabling asynchronous method execution support.</li>
 * </ol>
 * </p>
 */
@SpringBootApplication
@EnableAsync
public class Main {

	/**
	 * Main method invoked by the JVM to start the application.
	 * <p>
	 * First loads any environment variables defined in the specified <code>.env</code> file,
	 * then launches the Spring Boot application context.
	 * </p>
	 *
	 * @param args command-line arguments (ignored by this application)
	 */
	public static void main(String[] args) {
		// Load .env file before starting Spring Boot
		EnvLoader.loadEnvFile(".env");  // pass the relative path to your .env

		// Then run Spring Boot
		SpringApplication.run(Main.class, args);
	}
	}

