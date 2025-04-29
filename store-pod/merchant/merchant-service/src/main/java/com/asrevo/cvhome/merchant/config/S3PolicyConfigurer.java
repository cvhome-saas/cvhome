package com.asrevo.cvhome.merchant.config;

import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyResponse;

@Configuration
@AllArgsConstructor
@Slf4j
public class S3PolicyConfigurer implements ApplicationListener<ApplicationReadyEvent> {
    private final S3Client s3Client;
    private final CdnStorageProperties cdnStorageProperties;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        configurePolicy();
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
        String finalPolicy = policy
                .replace("${BUCKET}", cdnStorageProperties.bucket());
        try {
            PutBucketPolicyResponse putBucketPolicyResponse = s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(cdnStorageProperties.bucket())
                    .policy(
                            finalPolicy
                    )
                    .build());
            System.out.println(putBucketPolicyResponse);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
