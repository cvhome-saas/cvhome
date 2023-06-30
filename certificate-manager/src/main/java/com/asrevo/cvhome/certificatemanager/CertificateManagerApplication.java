package com.asrevo.cvhome.certificatemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootApplication
public class CertificateManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificateManagerApplication.class, args);
    }

    @Bean
    public AwsRegionProvider awsRegionProvider() {
        return () -> Region.EU_CENTRAL_1;
    }

    @Bean
    public S3Client s3Client(AwsRegionProvider regionProvider) {
        return S3Client.builder().region(regionProvider.getRegion()).build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient(AwsRegionProvider regionProvider) {
        return DynamoDbClient.builder().region(regionProvider.getRegion()).build();
    }
}
