package com.asrevo.cvhome.crypto.autoconfigure;

import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.crypto.SecretCryptoProviderRegistry;
import com.asrevo.cvhome.crypto.aws.AwsKmsCryptoProvider;
import com.asrevo.cvhome.crypto.caffeine.CachingSecretCryptoProvider;
import com.asrevo.cvhome.crypto.local.EnvironmentVariableKeyProvider;
import com.asrevo.cvhome.crypto.local.FileSystemKeyProvider;
import com.asrevo.cvhome.crypto.local.LocalAesCryptoProvider;
import com.asrevo.cvhome.crypto.local.LocalKeyProvider;
import com.asrevo.cvhome.crypto.local.StaticKeyProvider;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecretCryptoProperties.class)
public class SecretCryptoAutoConfiguration {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String DEFAULT_FILE_PATH = "${user.home}/.cvhome/secret-crypto/keys";
    private static final String KEY_ID = "default-key";
    @Bean
    @Primary
    public SecretCryptoProvider secretCryptoProvider(SecretCryptoProperties properties,
                                                     List<SecretCryptoProvider> customProviders) {
        List<SecretCryptoProvider> providers = new ArrayList<>(customProviders);
        tryCreateLocal(properties.getLocal()).ifPresent(providers::add);
        tryCreateAws(properties.getAws()).ifPresent(providers::add);

        String activeProviderId = resolveActiveProviderId(properties.getType());
        SecretCryptoProvider activeProvider = providers.stream()
                .filter(p -> p.providerId().equals(activeProviderId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Configured active crypto provider type " + properties.getType() + " is not available"
                                + " (failed to construct or missing configuration)"));

        SecretCryptoProvider registry = new SecretCryptoProviderRegistry(providers, activeProvider);
        return new CachingSecretCryptoProvider(registry, properties.getCache().getDuration());
    }

    private String resolveActiveProviderId(String type) {
        return switch (type) {
            case "LOCAL" -> LocalAesCryptoProvider.PROVIDER_ID;
            case "AWS" -> AwsKmsCryptoProvider.PROVIDER_ID;
            default -> type;
        };
    }

    private Optional<SecretCryptoProvider> tryCreateLocal(SecretCryptoProperties.LocalProperties local) {
        try {
            LocalKeyProvider keyProvider = createKeyProvider(local);
            return Optional.of(new LocalAesCryptoProvider(KEY_ID, keyProvider));
        } catch (Exception e) {
            log.warn("Local AES crypto provider unavailable, skipping: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<SecretCryptoProvider> tryCreateAws(SecretCryptoProperties.AwsProperties aws) {
        if (aws.getRegion() == null || aws.getKeyId() == null) {
            return Optional.empty();
        }
        try {
            KmsClient kmsClient = KmsClient.builder()
                    .region(Region.of(aws.getRegion()))
                    .build();
            return Optional.of(new AwsKmsCryptoProvider(kmsClient, aws.getKeyId()));
        } catch (Exception e) {
            log.warn("AWS KMS crypto provider unavailable, skipping: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private LocalKeyProvider createKeyProvider(SecretCryptoProperties.LocalProperties local) {
        return switch (local.getKeyProviderType()) {
            case STATIC -> {
                String hexKey = Optional.ofNullable(local.getKey()).orElseGet(SecretCryptoAutoConfiguration::createDefaultKey);
                yield new StaticKeyProvider(Map.of(KEY_ID, HexFormat.of().parseHex(hexKey)));
            }
            case ENV -> new EnvironmentVariableKeyProvider();
            case FILE -> {
                String path = Optional.ofNullable(local.getFilePath()).orElse(DEFAULT_FILE_PATH);
                yield new FileSystemKeyProvider(Paths.get(path), local.isFileBase64());
            }
        };
    }

    private static @NonNull String createDefaultKey() {
        byte[] randomKey = new byte[32];
        secureRandom.nextBytes(randomKey);
        return HexFormat.of().formatHex(randomKey);
    }
}