package com.asrevo.cvhome.crypto.autoconfigure;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.asrevo.cvhome.crypto")
public class SecretCryptoProperties {

    /**
     * Id of the active SecretCryptoProvider used for encryption: "LOCAL", "AWS", or the
     * providerId() of a custom SecretCryptoProvider bean.
     */
    private String type = "LOCAL";

    private LocalProperties local = new LocalProperties();
    private AwsProperties aws = new AwsProperties();
    private CacheProperties cache = new CacheProperties();

    @Getter
    @Setter
    public static class LocalProperties {
        /**
         * Key provider type: STATIC, ENV, FILE.
         */
        private KeyProviderType keyProviderType = KeyProviderType.STATIC;

        /**
         * The hex-encoded AES key (STATIC provider only).
         */
        private String key;

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
         * Cache expiration duration.
         */
        private Duration duration = Duration.ofMinutes(10);
    }
}