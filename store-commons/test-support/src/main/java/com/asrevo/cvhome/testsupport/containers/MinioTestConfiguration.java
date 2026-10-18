package com.asrevo.cvhome.testsupport.containers;

import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import com.asrevo.cvhome.commons.domain.StorageProviderType;

/**
 * A MinIO container bound to the {@code com.asrevo.cvhome.cdn.*} properties, for services that store media.
 *
 * <p>
 * MinIO no longer publishes {@code minio/minio} anonymously: Docker Hub stopped serving it first, then quay.io withdrew
 * anonymous pulls, which failed every storage test on a runner without the image cached. {@code bitnamilegacy/minio}
 * is Bitnami's frozen archive and still pulls. It runs as uid 1001, which cannot write the {@code /data} directory
 * {@link MinIOContainer} serves, so the container runs as root. {@link MinIOContainer} checks the name against
 * {@code minio/minio}, hence the declared substitute.
 * </p>
 */
@TestConfiguration(proxyBeanMethods = false)
public class MinioTestConfiguration {

    public static final String IMAGE = "bitnamilegacy/minio:2025.4.22";

    private static final String BUCKET = UUID.randomUUID().toString();

    private static final String REGION = "eu-central-1";

    @Bean(destroyMethod = "stop")
    MinIOContainer minioContainer() {
        MinIOContainer minio = new MinIOContainer(DockerImageName.parse(IMAGE).asCompatibleSubstituteFor("minio/minio"))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser("0"));
        minio.start();
        return minio;
    }

    @Bean
    DynamicPropertyRegistrar cdnProperties(MinIOContainer container) {
        return registry -> {
            registry.add("com.asrevo.cvhome.cdn.base-path", () -> String.format("%s/%s", container.getS3URL(), BUCKET));
            registry.add("com.asrevo.cvhome.cdn.storage.bucket", () -> BUCKET);
            registry.add("com.asrevo.cvhome.cdn.storage.provider", () -> StorageProviderType.MINIO);
            registry.add("com.asrevo.cvhome.cdn.storage.region", () -> REGION);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-url", container::getS3URL);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-access-key", container::getUserName);
            registry.add("com.asrevo.cvhome.cdn.storage.s3-secret-key", container::getPassword);
        };
    }

}
