package com.asrevo.cvhome.testsupport.containers;

import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MinIOContainer;

import com.asrevo.cvhome.commons.domain.StorageProviderType;

/**
 * A MinIO container bound to the {@code com.asrevo.cvhome.cdn.*} properties, for services that store media.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MinioTestConfiguration {

    public static final String IMAGE = "minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1";

    private static final String BUCKET = UUID.randomUUID().toString();

    private static final String REGION = "eu-central-1";

    @Bean(destroyMethod = "stop")
    MinIOContainer minioContainer() {
        MinIOContainer minio = new MinIOContainer(IMAGE);
        minio.start();
        return minio;
    }

    @Bean
    DynamicPropertyRegistrar cdnProperties(MinIOContainer container) {
        return registry -> {
            registry.add("com.asrevo.cvhome.cdn.basePath", () -> String.format("%s/%s", container.getS3URL(), BUCKET));
            registry.add("com.asrevo.cvhome.cdn.storage.bucket", () -> BUCKET);
            registry.add("com.asrevo.cvhome.cdn.storage.provider", () -> StorageProviderType.MINIO);
            registry.add("com.asrevo.cvhome.cdn.storage.region", () -> REGION);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-url", container::getS3URL);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-access-key", container::getUserName);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-secret-key", container::getPassword);
        };
    }

}
