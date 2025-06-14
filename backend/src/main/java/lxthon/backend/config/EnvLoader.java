package lxthon.backend.config;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Utility class for loading environment variables from a simple “.env” file.
 * <p>
 * Each line in the file should be in the format <code>KEY=VALUE</code>. Lines
 * beginning with <code>#</code> are treated as comments and ignored. For every
 * valid entry, this loader sets a corresponding system property via
 * {@link System#setProperty(String, String)}.
 * </p>
 */
public class EnvLoader {

    /**
     * Reads the specified file line by line, parses <code>KEY=VALUE</code> pairs,
     * and sets them as JVM system properties.
     * <p>
     * - Blank lines or lines not containing <code>=</code> are skipped.
     * - Lines starting with <code>#</code> (after trimming) are treated as comments.
     * - If a line splits into exactly two parts around the first <code>=</code>, the
     *   left-hand side is used as the property name and the right-hand side as its value.
     * </p>
     *
     * @param path the filesystem path to the .env file (e.g., <code>"config/.env"</code>)
     */
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
