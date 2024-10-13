package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.s2s.model.StorageProviderType;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.modules.cms.s3.S3StaticContentAssetsManagerImpl;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

@Configuration
@Slf4j
public class S3Config {
    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(CdnStorageProperties properties) throws URISyntaxException {
        if (StorageProviderType.MINIO.equals(properties.provider())) {
            S3Client s3Client =
                    S3Client.builder()
                            .endpointOverride(new URI(properties.s3Url()))
                            .serviceConfiguration(
                                    e ->
                                            e.pathStyleAccessEnabled(true)
                                                    .chunkedEncodingEnabled(false))
                            .region(Region.of(properties.region()))
                            .credentialsProvider(
                                    StaticCredentialsProvider.create(
                                            AwsBasicCredentials.create(
                                                    properties.s3AccessKey(),
                                                    properties.s3SecretKey())))
                            .build();
            new MinioInitializer(s3Client, properties).configurePolicy();
            return s3Client;
        } else {
            return S3Client.create();
        }
    }

    @Bean
    public ContentAssetsManager staticContentFileManager(
            S3Client s3Client, CdnStorageProperties properties) {
        return new S3StaticContentAssetsManagerImpl(s3Client, properties);
    }

    private record MinioInitializer(S3Client s3Client, CdnStorageProperties cdnStorageProperties) {

        public void configurePolicy() {
            String policy =
                    """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Principal": "*",
                                "Action": "s3:GetObject",
                                "Resource": [
                                    "arn:aws:s3:::${BUCKET}/*"
                                ],
                                "Effect": "Allow"
                            }
                        ]
                    }
                    """;
            String finalPolicy = policy.replace("${BUCKET}", cdnStorageProperties.bucket());
            try {
                s3Client.putBucketPolicy(
                        PutBucketPolicyRequest.builder()
                                .bucket(cdnStorageProperties.bucket())
                                .policy(finalPolicy)
                                .build());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
