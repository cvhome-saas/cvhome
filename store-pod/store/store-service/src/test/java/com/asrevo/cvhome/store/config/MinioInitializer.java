package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

@Configuration
public class MinioInitializer implements ApplicationListener<ApplicationReadyEvent>, Ordered {
    private final S3Client s3Client;
    private final CdnStorageProperties cdnStorageProperties;

    MinioInitializer(S3Client s3Client, CdnStorageProperties cdnStorageProperties) {
        this.s3Client = s3Client;
        this.cdnStorageProperties = cdnStorageProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String policy = """
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
        String finalPolicy = policy
                .replace("${BUCKET}", cdnStorageProperties.bucket());
        try {
            s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(cdnStorageProperties.bucket())
                    .policy(
                            finalPolicy
                    )
                    .build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
