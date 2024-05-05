package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.content.ContentService;
import com.asrevo.cvhome.store.core.services.merchant.MerchantStoreService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Configuration
@Slf4j
public class DefaultStoreAssetsInitializer implements ApplicationListener<ApplicationReadyEvent>, Ordered {
    private final CdnStorageProperties storageProperties;
    private final MerchantStoreService merchantStoreService;
    private final ProductService productService;
    private final ResourceLoader resourceLoader;
    private final ContentService contentService;
    private final ProductFileManager productFileManager;

    public DefaultStoreAssetsInitializer(CdnStorageProperties storageProperties, MerchantStoreService merchantStoreService, ProductService productService, ResourceLoader resourceLoader, ContentService contentService, ProductFileManager productFileManager) {
        this.storageProperties = storageProperties;
        this.merchantStoreService = merchantStoreService;
        this.productService = productService;
        this.resourceLoader = resourceLoader;
        this.contentService = contentService;
        this.productFileManager = productFileManager;
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        MerchantStore defaultStore = merchantStoreService.getDefaultStore();
        log.info("banner {}", defaultStore.getStoreBanner());
        log.info("logo {}", defaultStore.getStoreLogo());
        InputContentFile bannerFileInput = getStoreInputFile(defaultStore.getStoreBanner(), FileContentType.BANNER);
        contentService.addBanner(defaultStore.getCode(), bannerFileInput);
        InputContentFile logoFileInput = getStoreInputFile(defaultStore.getStoreLogo(), FileContentType.LOGO);
        contentService.addLogo(defaultStore.getCode(), logoFileInput);
        List<Product> products = productService.listByStore(defaultStore);
        for (Product product : products) {
            log.info("main image {} , for {} with id {}", product.getProductImage().getProductImage(), product.getId(), product.getProductImage().getId());

            productFileManager.addProductImage(product.getProductImage(), getStoreProductInputImage(product.getProductImage()));

            Set<ProductImage> images = product.getImages();
            for (ProductImage productImage : images) {
                log.info("sub image {} , for {} with id {} ", productImage.getProductImage(), product.getId(), productImage.getId());
                productFileManager.addProductImage(productImage, getStoreProductInputImage(productImage));
            }
        }

        log.info("will init DefaultStoreAssets {} {}", storageProperties.bucket(), storageProperties.provider());
    }

    @SneakyThrows
    private ImageContentFile getStoreInputFile(String name, FileContentType contentType) {
        ImageContentFile file = new ImageContentFile();
        Resource resource = resourceLoader.getResource("classpath:/assets/default-store/" + name);
        file.setFile(resource.getInputStream());
        file.setFileName(name);
        file.setMimeType(Files.probeContentType(Paths.get(name)));
        file.setFileContentType(contentType);
        return file;
    }

    @SneakyThrows
    private ImageContentFile getStoreProductInputImage(ProductImage productImage) {
        return getStoreInputFile(productImage.getProductImage(), FileContentType.PRODUCT);
    }


    @Override
    public int getOrder() {
        return 10;
    }
}
