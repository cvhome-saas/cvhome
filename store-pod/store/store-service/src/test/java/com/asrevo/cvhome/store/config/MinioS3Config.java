package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.store.MinIOContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URISyntaxException;
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
        Supplier<Object> getS3URL = () -> container.getS3URL() + "/" + bucket;
        properties.add("com.asrevo.cvhome.cdn.basePath", getS3URL);
        properties.add("com.asrevo.cvhome.cdn.storage.bucket", () -> bucket);
        properties.add("com.asrevo.cvhome.cdn.storage.provider", () -> "minio");
        return container;
    }

    @Bean
    public S3Client s3Client(MinIOContainer minIOContainer) throws URISyntaxException {
        return S3Client.builder().endpointOverride(new URI(minIOContainer.getS3URL()))
                .serviceConfiguration(e ->
                        e
                                .pathStyleAccessEnabled(true)
                                .chunkedEncodingEnabled(false)
                )
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create(minIOContainer.getAccessKey(), minIOContainer.getSecretKey())))
                .build();
    }

}
