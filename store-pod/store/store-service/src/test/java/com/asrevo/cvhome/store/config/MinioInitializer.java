package com.asrevo.cvhome.store.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class MinioInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final S3Client s3Client;

    MinioInitializer(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
    }
}
