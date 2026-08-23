package com.asrevo.cvhome.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.modules.cms.s3.S3StaticContentAssetsManagerImpl;

import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class ContentAssetsConfig {

    @Bean
    public ContentAssetsManager staticContentFileManager(S3Client s3Client, CdnStorageProperties properties) {
        return new S3StaticContentAssetsManagerImpl(s3Client, properties.bucket());
    }

}
