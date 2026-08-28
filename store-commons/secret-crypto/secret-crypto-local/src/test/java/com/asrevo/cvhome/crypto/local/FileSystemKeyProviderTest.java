package com.asrevo.cvhome.crypto.local;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The key provider a self-hosted deployment actually uses: a file under {@code ~/.cvhome/secret-crypto/keys}.
 *
 * <p>
 * Every unreadable-key path returns empty rather than throwing, which is what lets the autoconfiguration fall back
 * to a random key. That fallback is why these tests exist: a silently empty result here becomes a process that
 * encrypts happily and cannot read anything it wrote last time.
 * </p>
 */
class FileSystemKeyProviderTest {

    private static final String KEY_FILE = "default-key";

    private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    @Nested
    class ExpandSystemProperties {

        @Test
        void aPathWithoutAPlaceholderIsLeftAlone() {
            Path path = Paths.get("/etc/cvhome/keys");

            assertThat(FileSystemKeyProvider.expandSystemProperties(path)).isEqualTo(path);
        }

        @Test
        void aPlaceholderIsReplacedWithTheSystemPropertyItNames() {
            Path expanded = FileSystemKeyProvider.expandSystemProperties(Paths.get("${user.home}/.cvhome/keys"));

            assertThat(expanded).isEqualTo(Paths.get(System.getProperty("user.home"), ".cvhome/keys"));
        }

        @Test
        void anUnknownPropertyIsRefusedInsteadOfExpandingToTheLiteralPlaceholder() {
            Path path = Paths.get("${no.such.property}/keys");

            assertThatThrownBy(() -> FileSystemKeyProvider.expandSystemProperties(path))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no.such.property");
        }
    }

    @Nested
    class GetKey {

        @Test
        void aMissingKeyFileReadsAsAbsent(@TempDir Path dir) {
            assertThat(new FileSystemKeyProvider(dir).getKey()).isEmpty();
        }

        @Test
        void aDirectoryWhereTheKeyFileShouldBeReadsAsAbsent(@TempDir Path dir) throws IOException {
            Files.createDirectory(dir.resolve(KEY_FILE));

            assertThat(new FileSystemKeyProvider(dir).getKey()).isEmpty();
        }

        @Test
        void rawKeyBytesAreReturnedAsWritten(@TempDir Path dir) throws IOException {
            Files.write(dir.resolve(KEY_FILE), KEY);

            assertThat(new FileSystemKeyProvider(dir).getKey()).hasValue(KEY);
        }

        @Test
        void aBase64KeyIsDecodedAndSurroundingWhitespaceIgnored(@TempDir Path dir) throws IOException {
            Files.writeString(dir.resolve(KEY_FILE), String.format("%s%n", Base64.getEncoder().encodeToString(KEY)));

            assertThat(new FileSystemKeyProvider(dir, true).getKey()).hasValue(KEY);
        }

        @Test
        void aFileThatIsNotBase64ReadsAsAbsentRatherThanThrowing(@TempDir Path dir) throws IOException {
            Files.writeString(dir.resolve(KEY_FILE), "not base64 !!!");

            assertThat(new FileSystemKeyProvider(dir, true).getKey()).isEmpty();
        }
    }
}
