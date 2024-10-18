package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.cdn.storage")
public record CdnStorageProperties(
        String bucket,
        StorageProviderType provider,
        String s3Url,
        String s3AccessKey,
        String s3SecretKey,
        String region) {}
