package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.store.core.modules.cms.content.StaticContentFileManager;
import com.asrevo.cvhome.store.core.modules.cms.content.StaticContentFileManagerImpl;
import com.asrevo.cvhome.store.core.modules.cms.content.local.CmsStaticContentFileManagerImpl;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManagerImpl;
import com.asrevo.cvhome.store.core.modules.cms.product.local.CmsImageFileManagerImpl;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.LabelUtils;
import com.asrevo.cvhome.store.utils.LocalImageFilePathUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class TempConfig {
    @Bean
    public ProductFileManager productFileManager() {
        CmsImageFileManagerImpl instance = CmsImageFileManagerImpl.getInstance();
        ProductFileManagerImpl productFileManager = new ProductFileManagerImpl();
        productFileManager.setUploadImage(instance);
        productFileManager.setGetImage(instance);
        productFileManager.setRemoveImage(instance);
        return productFileManager;
    }

    @Bean
    public StaticContentFileManager staticContentFileManager() {
        CmsStaticContentFileManagerImpl cmsStaticContentFileManager = new CmsStaticContentFileManagerImpl();
        StaticContentFileManagerImpl fileManager = new StaticContentFileManagerImpl();
        fileManager.setGetFile(cmsStaticContentFileManager);
        fileManager.setRemoveFile(cmsStaticContentFileManager);
        fileManager.setUploadFile(cmsStaticContentFileManager);
        fileManager.setAddFolder(cmsStaticContentFileManager);
        fileManager.setListFolder(cmsStaticContentFileManager);
        fileManager.setRemoveFolder(cmsStaticContentFileManager);
        return fileManager;
    }

    @Bean("img")
    public ImageFilePath img() {
        LocalImageFilePathUtils localImageFilePathUtils = new LocalImageFilePathUtils();
        localImageFilePathUtils.setBasePath("/static");
        return localImageFilePathUtils;
    }

    @Bean("shopizer-properties")
    public Properties shopizerProperties() {
        Properties properties = new Properties();
        properties.put("MULTIPLE_PRICE_AVAILABILITY", false);
        properties.put("INDEX_PRODUCTS", false);
        properties.put("SHOP_SCHEME", "http");
        properties.put("CONTEXT_PATH", "sm-shop");
        properties.put("PRODUCT_IMAGE_WIDTH_SIZE", 350);
        properties.put("PRODUCT_IMAGE_HEIGHT_SIZE", 375);
        properties.put("CROP_UPLOADED_IMAGES", false);
        properties.put("PRODUCT_IMAGE_MAX_HEIGHT_SIZE", 2000);
        properties.put("PRODUCT_IMAGE_MAX_WIDTH_SIZE", 4000);
        properties.put("PRODUCT_IMAGE_MAX_SIZE", 9000000);
        properties.put("IMAGE_FORMATS", "jpg|png|gif");
        properties.put("POPULATE_TEST_DATA", true);
        return properties;
    }
    @Bean
    public LabelUtils messages() {
        return new LabelUtils();
    }
}
