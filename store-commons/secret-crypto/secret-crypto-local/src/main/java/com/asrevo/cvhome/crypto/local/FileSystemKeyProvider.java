package com.asrevo.cvhome.crypto.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

/**
 * Key provider that reads keys from the file system.
 * Keys are expected to be stored in files within a specified directory.
 * The file name should match the key ID.
 * Supports both raw binary keys and Base64 encoded keys.
 */
public class FileSystemKeyProvider implements LocalKeyProvider {

    private final Path keysDirectory;
    private final boolean base64Encoded;

    public FileSystemKeyProvider(Path keysDirectory) {
        this(keysDirectory, false);
    }

    public FileSystemKeyProvider(Path keysDirectory, boolean base64Encoded) {
        this.keysDirectory = keysDirectory;
        this.base64Encoded = base64Encoded;
    }

    @Override
    public Optional<byte[]> getKey(String keyId) {
        Path keyFile = keysDirectory.resolve(keyId);
        if (!Files.exists(keyFile) || !Files.isRegularFile(keyFile)) {
            return Optional.empty();
        }

        try {
            byte[] content = Files.readAllBytes(keyFile);
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
