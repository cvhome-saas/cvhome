package com.asrevo.cvhome.crypto.autoconfigure;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.asrevo.cvhome.crypto")
public class SecretCryptoProperties {

    /**
     * Crypto provider type: LOCAL or AWS.
     */
    private ProviderType type = ProviderType.LOCAL;

    private LocalProperties local = new LocalProperties();
    private AwsProperties aws = new AwsProperties();
    private CacheProperties cache = new CacheProperties();

    public enum ProviderType {
        LOCAL, AWS
    }

    @Getter
    @Setter
    public static class LocalProperties {
        /**
         * The active key ID to be used for encryption.
         */
        private String activeKeyId;

        /**
         * Key provider type: STATIC, ENV, FILE.
         */
        private KeyProviderType keyProviderType = KeyProviderType.STATIC;

        /**
         * Static keys map (ID to Base64-encoded key).
         */
        private Map<String, String> keys;

        /**
         * Directory path for file-based key provider.
         */
        private String filePath;

        /**
         * Whether the files in filePath are Base64 encoded.
         */
        private boolean fileBase64 = false;

        public enum KeyProviderType {
            STATIC, ENV, FILE
        }
    }

    @Getter
    @Setter
    public static class AwsProperties {
        /**
         * AWS KMS Key ID or ARN.
         */
        private String keyId;

        /**
         * AWS Region.
         */
        private String region;
    }

    @Getter
    @Setter
    public static class CacheProperties {
        /**
         * Whether to enable caching for decryption.
         */
        private boolean enabled = false;

        /**
         * Cache expiration duration.
         */
        private Duration duration = Duration.ofMinutes(10);
    }
}