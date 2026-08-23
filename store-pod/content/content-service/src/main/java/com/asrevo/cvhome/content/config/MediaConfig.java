package com.asrevo.cvhome.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.content.storage.MediaStorage;
import com.asrevo.cvhome.s2s.model.CdnProperties;
import com.asrevo.cvhome.s2s.model.CdnStorageProperties;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * The media library's object storage: the pod bucket and the public base path the CDN (or MinIO) serves it from.
 */
@Configuration
public class MediaConfig {

    @Bean
    public MediaStorage mediaStorage(S3Client s3Client, CdnStorageProperties storage, CdnProperties cdn) {
        return new MediaStorage(s3Client, storage.bucket(), cdn.basePath());
    }

}
