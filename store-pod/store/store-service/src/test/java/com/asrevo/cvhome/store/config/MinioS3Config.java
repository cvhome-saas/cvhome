package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.store.MinIOContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class MinioS3Config {
    @Bean
    public MinIOContainer minIOContainer(DynamicPropertyRegistry properties) {
        MinIOContainer container = new MinIOContainer("bitnami/minio")
                .withEnv("MINIO_DEFAULT_BUCKETS", "cvhome");
        container.start();
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
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create(minIOContainer.getAccessKey(), minIOContainer.getSecretKey())))
                .build();
    }

}
