package com.asrevo.cvhome.merchant.service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AssetsInitService {
    private final ResourceLoader resourceLoader;
    private final StoreFacade storeFacade;

    @SneakyThrows
    public void loadAssets() {
        Resource[] resources =
                ((AnnotationConfigServletWebServerApplicationContext) resourceLoader)
                        .getResources("classpath:/assets/**");
        Arrays.stream(resources)
                .filter(resource -> !isDirectory(resource))
                .forEach(
                        r -> {
                            if (r.toString().contains("/logo/")) {
                                uploadLogoFile(r);
                            }
                            if (r.toString().contains("/banner/")) {
                                uploadBanner(r);
                            }
                            if (r.toString().contains("/slider/")) {
                                uploadSlider(r);
                            }
                        });
    }

    @SneakyThrows
    private void uploadSlider(Resource r) {
        Path p = toPath(r);
        StoreMerchantId storeMerchantId;
        FileContentType type = FileContentType.SLIDER;
        String fileName = p.getFileName().toString();
        storeMerchantId = new StoreMerchantId(p.getParent().getParent().getFileName().toString());
        log.info("Store merchant id: {} , slider: {}", storeMerchantId, fileName);
        ImageContentFile content = loadFile(r, type);
        storeFacade.addSlider(storeMerchantId.storeMerchantId(), content);
    }

    @SneakyThrows
    private void uploadBanner(Resource r) {
        Path p = toPath(r);
        StoreMerchantId storeMerchantId;
        FileContentType type = FileContentType.BANNER;
        String fileName = p.getFileName().toString();
        storeMerchantId = new StoreMerchantId(p.getParent().getParent().getFileName().toString());
        log.info("Store merchant id: {} , banner: {}", storeMerchantId, fileName);
        ImageContentFile content = loadFile(r, type);
        storeFacade.addBanner(storeMerchantId.storeMerchantId(), content);
    }

    @SneakyThrows
    private void uploadLogoFile(Resource r) {
        Path p = toPath(r);
        StoreMerchantId storeMerchantId;
        FileContentType type = FileContentType.LOGO;
        String fileName = p.getFileName().toString();
        storeMerchantId = new StoreMerchantId(p.getParent().getParent().getFileName().toString());
        log.info("Store merchant id: {} , logo: {}", storeMerchantId, fileName);
        ImageContentFile content = loadFile(r, type);
        storeFacade.addLogo(storeMerchantId.storeMerchantId(), content);
    }

    @SneakyThrows
    private ImageContentFile loadFile(Resource resource, FileContentType contentType) {
        ImageContentFile file = new ImageContentFile();
        file.setFile(resource.getInputStream());
        file.setFileName(resource.getFilename());
        file.setMimeType(
                Files.probeContentType(Paths.get(Objects.requireNonNull(resource.getFilename()))));
        file.setFileContentType(contentType);
        return file;
    }

    @SneakyThrows
    private static Path toPath(Resource resource) {
        return resource.getFile().toPath();
    }

    @SneakyThrows
    boolean isDirectory(Resource resource) {
        return Files.isDirectory(resource.getFile().toPath());
    }
}
