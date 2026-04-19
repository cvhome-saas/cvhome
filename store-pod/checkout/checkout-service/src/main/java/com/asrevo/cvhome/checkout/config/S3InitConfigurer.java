package com.asrevo.cvhome.checkout.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.commons.domain.StorageProviderType;
import com.asrevo.cvhome.s2s.model.CdnStorageProperties;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class S3InitConfigurer implements ApplicationListener<ApplicationReadyEvent> {

    private final S3Client s3Client;

    private final CdnStorageProperties cdnStorageProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (StorageProviderType.MINIO.equals(cdnStorageProperties.provider())) {
            configureBucket();
            configurePolicy();
        }
    }

    private void configureBucket() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(cdnStorageProperties.bucket()).build());
        } catch (Exception e) {
            log.error("error creating bucket", e);
        }
    }

    public void configurePolicy() {
        String policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Sid": "AllowPublicRead",
                            "Effect": "Allow",
                            "Principal": {
                                "AWS": "*"
                            },
                            "Action": "s3:GetObject",
                            "Resource": "arn:aws:s3:::${BUCKET}/*"
                        }
                    ]
                }
                """;
        String finalPolicy = policy.replace("${BUCKET}", cdnStorageProperties.bucket());
        try {
            = s3Client.putBucketPolicy(
                    PutBucketPolicyRequest.builder().bucket(cdnStorageProperties.bucket()).policy(finalPolicy).build());
        } catch (Exception e) {
            log.error("error putting policy", e);
        }
    }

}
