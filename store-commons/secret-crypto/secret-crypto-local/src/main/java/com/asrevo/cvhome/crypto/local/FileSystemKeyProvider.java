package com.asrevo.cvhome.crypto.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Key provider that reads keys from the file system.
 * Keys are expected to be stored in files within a specified directory.
 * The file name should match the key ID.
 * Supports both raw binary keys and Base64 encoded keys.
 */
public class FileSystemKeyProvider implements LocalKeyProvider {

    private final Path keysDirectory;
    private final boolean base64Encoded;
    private static final String DEFAULT_KEY_NAME = "default-key";

    public FileSystemKeyProvider(Path keysDirectory) {
        this(keysDirectory, false);
    }

    public FileSystemKeyProvider(Path keysDirectory, boolean base64Encoded) {
        this.keysDirectory = keysDirectory;
        this.base64Encoded = base64Encoded;
    }

    public static Path expandSystemProperties(Path input) {
        String path = input.toString();

        Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
        Matcher matcher = pattern.matcher(path);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String property = matcher.group(1);
            String value = System.getProperty(property);
            if (value == null) {
                throw new IllegalArgumentException("Unknown system property: " + property);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return Paths.get(result.toString());
    }

    @Override
    public Optional<byte[]> getKey() {
        Path keyFile = keysDirectory.resolve(DEFAULT_KEY_NAME);
        Path path = expandSystemProperties(keyFile);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }

        try {
            byte[] content = Files.readAllBytes(path);
            if (base64Encoded) {
                return Optional.of(Base64.getDecoder().decode(new String(content).trim()));
            }
            return Optional.of(content);
        } catch (IOException | IllegalArgumentException e) {
            // Log error or handle appropriately. For now, return empty to signify key not found/readable.
            return Optional.empty();
        }
    }
}
