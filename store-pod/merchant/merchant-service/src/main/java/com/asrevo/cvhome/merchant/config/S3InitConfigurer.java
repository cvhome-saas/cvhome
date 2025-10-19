package com.asrevo.cvhome.merchant.config;

import com.asrevo.cvhome.commons.domain.StorageProviderType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.service.AssetsInitService;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

@Configuration
@AllArgsConstructor
@Slf4j
public class S3InitConfigurer implements ApplicationListener<ApplicationReadyEvent> {
    private final S3Client s3Client;
    private final CdnStorageProperties cdnStorageProperties;
    private final AssetsInitService assetsInitService;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (StorageProviderType.MINIO.equals(cdnStorageProperties.provider())) {
            configureBucket();
            configurePolicy();
        }
        assetsInitService.loadAssets();
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
        String finalPolicy = policy
                .replace("${BUCKET}", cdnStorageProperties.bucket());
        try {
            PutBucketPolicyResponse putBucketPolicyResponse = s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(cdnStorageProperties.bucket())
                    .policy(
                            finalPolicy
                    )
                    .build());
        } catch (Exception e) {
            log.error("error putting policy", e);
        }
    }
}
