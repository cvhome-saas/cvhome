package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.s2s.model.StoreProductImageProperties;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManagerImpl;
import com.asrevo.cvhome.store.core.modules.cms.product.aws.S3ProductContentFileManager;
import com.asrevo.cvhome.store.utils.LabelUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TempConfig {
    @Bean
    public ProductFileManager productFileManager(S3ProductContentFileManager s3ProductContentFileManager, StoreProductImageProperties imageProperties) {
        return new ProductFileManagerImpl(s3ProductContentFileManager, s3ProductContentFileManager, s3ProductContentFileManager, imageProperties);
    }

    @Bean
    public LabelUtils messages() {
        return new LabelUtils();
    }
}
