package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.modules.cms.s3.S3StaticContentAssetsManagerImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client() {
        return S3Client.create();
    }

    @Bean
    public ContentAssetsManager staticContentFileManager(S3Client s3Client) {
        return new S3StaticContentAssetsManagerImpl(s3Client);
    }

}
