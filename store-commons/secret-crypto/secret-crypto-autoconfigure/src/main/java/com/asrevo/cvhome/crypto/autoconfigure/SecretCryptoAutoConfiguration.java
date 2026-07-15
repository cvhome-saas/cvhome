package com.asrevo.cvhome.crypto.autoconfigure;

import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.crypto.aws.AwsKmsCryptoProvider;
import com.asrevo.cvhome.crypto.caffeine.CachingSecretCryptoProvider;
import com.asrevo.cvhome.crypto.local.EnvironmentVariableKeyProvider;
import com.asrevo.cvhome.crypto.local.FileSystemKeyProvider;
import com.asrevo.cvhome.crypto.local.LocalAesCryptoProvider;
import com.asrevo.cvhome.crypto.local.LocalKeyProvider;
import com.asrevo.cvhome.crypto.local.StaticKeyProvider;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@AutoConfiguration
@EnableConfigurationProperties(SecretCryptoProperties.class)
public class SecretCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "com.asrevo.cvhome.crypto.type", havingValue = "LOCAL", matchIfMissing = true)
    public SecretCryptoProvider localAesCryptoProvider(SecretCryptoProperties properties) {
        SecretCryptoProperties.LocalProperties local = properties.getLocal();
        LocalKeyProvider keyProvider = createKeyProvider(local);
        SecretCryptoProvider provider = new LocalAesCryptoProvider(local.getActiveKeyId(), keyProvider);
        return wrapWithCache(provider, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "com.asrevo.cvhome.crypto.type", havingValue = "AWS")
    public SecretCryptoProvider awsKmsCryptoProvider(SecretCryptoProperties properties) {
        SecretCryptoProperties.AwsProperties aws = properties.getAws();
        KmsClient kmsClient = KmsClient.builder()
                .region(Region.of(aws.getRegion()))
                .build();
        SecretCryptoProvider provider = new AwsKmsCryptoProvider(kmsClient, aws.getKeyId());
        return wrapWithCache(provider, properties);
    }

    private SecretCryptoProvider wrapWithCache(SecretCryptoProvider provider, SecretCryptoProperties properties) {
        if (properties.getCache().isEnabled()) {
            return new CachingSecretCryptoProvider(provider, properties.getCache().getDuration());
        }
        return provider;
    }

    private LocalKeyProvider createKeyProvider(SecretCryptoProperties.LocalProperties local) {
        return switch (local.getKeyProviderType()) {
            case STATIC -> {
                Map<String, byte[]> keys = local.getKeys().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> Base64.getDecoder().decode(e.getValue())
                        ));
                yield new StaticKeyProvider(keys);
            }
            case ENV -> new EnvironmentVariableKeyProvider();
            case FILE -> new FileSystemKeyProvider(Paths.get(local.getFilePath()), local.isFileBase64());
        };
    }
}