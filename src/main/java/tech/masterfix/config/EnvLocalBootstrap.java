package tech.masterfix.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Loads .env.local into system properties before Spring starts. */
public final class EnvLocalBootstrap {

    private EnvLocalBootstrap() {}

    public static void load() {
        Path envFile = findEnvFile();
        if (envFile == null) {
            return;
        }
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
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            // optional local file
        }
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
}
