package lxthon.backend;

import lxthon.backend.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		// Load .env file before starting Spring Boot
		EnvLoader.loadEnvFile(".env");  // pass the relative path to your .env

		// Then run Spring Boot
		SpringApplication.run(Main.class, args);
	}
	}

