package com.asrevo.cvhome.checkout.config;

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

    private static final String DEFAULT_BUCKET = UUID.randomUUID().toString();

    private final Region region = Region.EU_CENTRAL_1;

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(MinIOContainer container) {
        return registry -> {
            Supplier<Object> getUiURL = () -> container.getS3URL() + "/" + DEFAULT_BUCKET;
            registry.add("com.asrevo.cvhome.cdn.basePath", getUiURL);
            registry.add("com.asrevo.cvhome.cdn.storage.bucket", () -> DEFAULT_BUCKET);
            registry.add("com.asrevo.cvhome.cdn.storage.provider", () -> StorageProviderType.MINIO);
            registry.add("com.asrevo.cvhome.cdn.storage.region", region::id);
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
