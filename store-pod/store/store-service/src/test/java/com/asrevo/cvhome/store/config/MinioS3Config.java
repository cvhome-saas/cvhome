package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.s2s.model.StorageProviderType;
import com.asrevo.cvhome.store.MinIOContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class MinioS3Config {
    @Bean
    public MinIOContainer minIOContainer(DynamicPropertyRegistry properties) {
        String bucket = UUID.randomUUID().toString();
        MinIOContainer container = new MinIOContainer("bitnami/minio")
                .withEnv("MINIO_DEFAULT_BUCKETS", bucket);
        container.start();
        Supplier<Object> getUiURL = () -> container.getUiURL() + "/" + bucket;
        properties.add("com.asrevo.cvhome.cdn.basePath", getUiURL);
        properties.add("com.asrevo.cvhome.cdn.storage.bucket", () -> bucket);
        properties.add("com.asrevo.cvhome.cdn.storage.provider", () -> StorageProviderType.MINIO);
        properties.add("com.asrevo.cvhome.cdn.storage.region", () -> "eu-central-1");
        properties.add("com.asrevo.cvhome.cdn.storage.s3-url", container::getS3URL);
        properties.add("com.asrevo.cvhome.cdn.storage.s3-access-key", container::getAccessKey);
        properties.add("com.asrevo.cvhome.cdn.storage.s3-secret-key", container::getSecretKey);
        return container;
    }

}
