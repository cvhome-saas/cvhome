package com.asrevo.cvhome.content.config;

import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MinIOContainer;

import com.asrevo.cvhome.commons.domain.StorageProviderType;

import software.amazon.awssdk.regions.Region;

@Configuration
public class MinioS3Config {

    private static final String BUCKET = UUID.randomUUID().toString();

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(MinIOContainer container) {
        return registry -> {
            Supplier<Object> basePath = () -> String.format("%s/%s", container.getS3URL(), BUCKET);
            registry.add("com.asrevo.cvhome.cdn.basePath", basePath);
            registry.add("com.asrevo.cvhome.cdn.storage.bucket", () -> BUCKET);
            registry.add("com.asrevo.cvhome.cdn.storage.provider", () -> StorageProviderType.MINIO);
            registry.add("com.asrevo.cvhome.cdn.storage.region", Region.EU_CENTRAL_1::id);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-url", container::getS3URL);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-access-key", container::getUserName);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-secret-key", container::getPassword);
        };
    }

    @Bean(destroyMethod = "stop")
    public MinIOContainer minioContainer() {
        MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1");
        minio.start();
        return minio;
    }

}
