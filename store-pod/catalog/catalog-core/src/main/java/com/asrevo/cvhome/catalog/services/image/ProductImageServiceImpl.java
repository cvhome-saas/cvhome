package com.asrevo.cvhome.catalog.services.image;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.errors.ProductImageNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductImageNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.repositories.ProductImageRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.ImageSizeMisconfiguredException;
import com.asrevo.cvhome.store.core.modules.cms.errors.ImageUnreadableException;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    private final ProductFileManager productFileManager;

    private final ImageMapper imageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReadableImage> list(StoreMerchantId store, Long productId) throws ProductNotFoundException {
        return imageMapper.toReadable(requireProduct(store, productId));
    }

    @Override
    @Transactional
    public void add(StoreMerchantId store, Long productId, MultipartFile[] files, int firstPosition,
                    boolean defaultImage) throws ProductNotFoundException, ProductImageNotPersistedException {
        Product product = requireProduct(store, productId);
        boolean needsDefault = defaultImage || product.getImages().stream().noneMatch(ProductImage::isDefaultImage);
        int position = firstPosition;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            ProductImage image = new ProductImage(product, file.getOriginalFilename(), position++, needsDefault);
            needsDefault = false;
            store(product, image, file);
            product.getImages().add(productImageRepository.save(image));
        }
    }

    private void store(Product product, ProductImage image, MultipartFile file)
            throws ProductImageNotPersistedException {
        ImageContentFile content = new ImageContentFile();
        content.setFileName(image.getProductImage());
        content.setFileContentType(FileContentType.PRODUCT);
        try (var stream = file.getInputStream()) {
            content.setFile(stream);
            productFileManager.addProductImage(cmsImage(product, image), content);
        } catch (IOException | AssetUploadFailedException | ImageUnreadableException
                 | ImageSizeMisconfiguredException e) {
            throw ProductImageNotPersistedException.of(product.getSku(), e);
        }
    }

    @Override
    @Transactional
    public void reorder(StoreMerchantId store, Long productId, Long imageId, int position)
            throws ProductImageNotFoundException {
        requireImage(store, productId, imageId).setSortOrder(position);
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Long productId, Long imageId) throws ProductImageNotFoundException {
        ProductImage image = requireImage(store, productId, imageId);
        removeFile(image);
        image.getProduct().getImages().remove(image);
        productImageRepository.delete(image);
    }

    /**
     * Drops every file of a product; the rows go with the product itself.
     */
    @Override
    public void removeFiles(Product product) {
        product.getImages().forEach(this::removeFile);
    }

    private void removeFile(ProductImage image) {
        if (image.isExternal() || image.getProductImage() == null) {
            return;
        }
        try {
            productFileManager.removeProductImage(cmsImage(image.getProduct(), image));
        } catch (AssetDeleteFailedException e) {
            // The row still goes: a file that could not be removed is an orphan on the CDN, not a broken product.
            log.warn("Could not remove image file {} of product {}", image.getProductImage(),
                    image.getProduct().getSku(), e);
        }
    }

    private static CmsProductImage cmsImage(Product product, ProductImage image) {
        return new CmsProductImage(product.getId(), product.getStore(), product.getSku(), image.getProductImage());
    }

    private Product requireProduct(StoreMerchantId store, Long productId) throws ProductNotFoundException {
        return productRepository.findByStoreAndId(store, productId)
                .orElseThrow(() -> ProductNotFoundException.of(productId, store));
    }

    private ProductImage requireImage(StoreMerchantId store, Long productId, Long imageId)
            throws ProductImageNotFoundException {
        return productImageRepository.findByStoreAndProductAndId(store, productId, imageId)
                .orElseThrow(() -> ProductImageNotFoundException.of(imageId, store));
    }
}
