package tech.masterfix.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads .env.local before Spring resolves ${RESEND_API_KEY} etc.
 * Works for IDE run, bootRun, and java -jar from project root.
 */
public class LocalEnvLoader implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LocalEnvLoader.class);
    private static final String SOURCE_NAME = "envLocal";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = findEnvFile();
        if (envFile == null) {
            return;
        }

        Map<String, Object> properties = loadEnvFile(envFile);
        if (properties.isEmpty()) {
            return;
        }

        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, properties));
        log.info("Loaded {} vars from {}", properties.size(), envFile.toAbsolutePath());
    }

    private static Path findEnvFile() {
        Path cwd = Path.of(System.getProperty("user.dir", "."));
        Path direct = cwd.resolve(".env.local");
        if (Files.isRegularFile(direct)) {
            return direct;
        }

        Path parent = cwd.getParent();
        if (parent != null) {
            Path parentFile = parent.resolve(".env.local");
            if (Files.isRegularFile(parentFile)) {
                return parentFile;
            }
        }
        return null;
    }

    private static Map<String, Object> loadEnvFile(Path envFile) {
        Map<String, Object> properties = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                properties.put(key, value);
            }
        } catch (IOException ignored) {
            return Map.of();
        }
        return properties;
    }
}
