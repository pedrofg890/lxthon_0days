package lxthon.backend.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

public class EnvLoader {
    public static void loadEnvFile(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            reader.lines()
                    .filter(line -> line.contains("=") && !line.trim().startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            System.setProperty(key, value); // Set as system property
                        }
                    });
            System.out.println(".env loaded from: " + path);
        } catch (Exception e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }
}
