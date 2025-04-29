package com.asrevo.cvhome.catalog.config;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

@Configuration
@Slf4j
public class DefaultStoreAssetsInitializer implements ApplicationListener<ApplicationReadyEvent>, Ordered {
    private final ResourceLoader resourceLoader;
    private final ProductFileManager productFileManager;

    public DefaultStoreAssetsInitializer(ResourceLoader resourceLoader, ProductFileManager productFileManager) {
        this.resourceLoader = resourceLoader;
        this.productFileManager = productFileManager;
    }

    @SneakyThrows
    private static Path toPath(Resource resource) {
        return resource.getFile().toPath();
    }

    @SneakyThrows
    boolean isDirectory(Resource resource) {
        return Files.isDirectory(resource.getFile().toPath());
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Resource[] resources = ((AnnotationConfigServletWebServerApplicationContext) resourceLoader).getResources("classpath:/assets/**");
        Arrays.stream(resources)
                .filter(resource -> !isDirectory(resource))
                .forEach(r -> {
                    if (r.toString().contains("/products/")) {
                        uploadProduct(r);
                    }
                });

    }

    @SneakyThrows
    private void uploadProduct(Resource r) {
        Path p = toPath(r);
        StoreMerchantId storeMerchantId;
        FileContentType type = FileContentType.PRODUCT;
        String fileName = p.getFileName().toString();
        String product = p.getParent().getFileName().toString();
        String productId = product.split("-")[0];
        String productSku = product.split("-")[1];
        storeMerchantId = new StoreMerchantId(p.getParent().getParent().getParent().getFileName().toString());
        log.info("Store merchant id: {} , product: {}", storeMerchantId, fileName);
        CmsProductImage pm = new CmsProductImage(Long.parseLong(productId), storeMerchantId, productSku, fileName);
        productFileManager.addProductImage(pm, loadFile(r, type));
    }

    @SneakyThrows
    private ImageContentFile loadFile(Resource resource, FileContentType contentType) {
        ImageContentFile file = new ImageContentFile();
        file.setFile(resource.getInputStream());
        file.setFileName(resource.getFilename());
        file.setMimeType(Files.probeContentType(Paths.get(Objects.requireNonNull(resource.getFilename()))));
        file.setFileContentType(contentType);
        return file;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
