package com.asrevo.cvhome.catalog.config;

import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.s2s.model.StoreProductImageProperties;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManagerImpl;
import com.asrevo.cvhome.store.core.modules.cms.product.aws.S3ProductContentFileManager;
import com.asrevo.cvhome.store.core.modules.cms.s3.S3StaticContentAssetsManagerImpl;
import com.asrevo.cvhome.store.utils.LabelUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class TempConfig {
    @Bean
    public S3ProductContentFileManager s3ProductContentFileManager(
            S3Client s3Client, CdnStorageProperties properties) {
        return new S3ProductContentFileManager(s3Client, properties.bucket());
    }

    @Bean
    public ProductFileManager productFileManager(
            S3ProductContentFileManager s3ProductContentFileManager,
            StoreProductImageProperties imageProperties) {
        return new ProductFileManagerImpl(
                s3ProductContentFileManager,
                s3ProductContentFileManager,
                s3ProductContentFileManager,
                imageProperties.cropUploads(),
                imageProperties.height(),
                imageProperties.width());
    }

    @Bean
    public ContentAssetsManager staticContentFileManager(
            S3Client s3Client, CdnStorageProperties properties) {
        return new S3StaticContentAssetsManagerImpl(s3Client, properties.bucket());
    }

    @Bean
    public LabelUtils messages() {
        return new LabelUtils();
    }
}
