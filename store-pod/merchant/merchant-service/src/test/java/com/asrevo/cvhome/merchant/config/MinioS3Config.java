package com.asrevo.cvhome.merchant.config;

import com.asrevo.cvhome.commons.domain.StorageProviderType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistrar;

import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class MinioS3Config {
    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(MinIOContainer container) {
        return registry -> {
            String bucket = container.getBucket();
            Supplier<Object> getUiURL = () -> container.getS3URL() + "/" + bucket;
            registry.add("com.asrevo.cvhome.cdn.basePath", getUiURL);
            registry.add("com.asrevo.cvhome.cdn.storage.bucket", () -> bucket);
            registry.add("com.asrevo.cvhome.cdn.storage.provider", () -> StorageProviderType.MINIO);
            registry.add("com.asrevo.cvhome.cdn.storage.region", () -> "eu-central-1");
            registry.add("com.asrevo.cvhome.cdn.storage.s3-url", container::getS3URL);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-access-key", container::getAccessKey);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-secret-key", container::getSecretKey);
        };
    }

    @Bean
    public MinIOContainer minIOContainer() {
        MinIOContainer container = new MinIOContainer(UUID.randomUUID().toString());
        container.start();
        return container;
    }

}
