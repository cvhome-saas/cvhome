package com.asrevo.cvhome.catalog.services.image;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import lombok.RequiredArgsConstructor;

/**
 * Turns image rows into the urls a browser fetches. A file image resolves to the CDN path under the product's sku;
 * an external image is served from wherever it lives.
 */
@Component
@RequiredArgsConstructor
public class ImageMapper {

    private final ImageFilePath imageFilePath;

    public ReadableImage toReadable(Product product, ProductImage image) {
        ReadableImage readable = new ReadableImage();
        readable.setId(image.getId());
        readable.setImageName(image.getProductImage());
        readable.setImageType(image.getImageType());
        readable.setOrder(image.getSortOrder());
        readable.setDefaultImage(image.isDefaultImage());
        readable.setExternalUrl(image.getProductImageUrl());
        if (image.isExternal()) {
            readable.setImageUrl(image.getProductImageUrl());
            readable.setVideoUrl(image.getProductImageUrl());
        } else {
            readable.setImageUrl(imageFilePath.getContextPath()
                    + imageFilePath.buildProductImageUtils(product.getStore(), product.getSku(),
                    image.getProductImage()));
        }
        return readable;
    }

    /**
     * All of a product's images, in display order.
     */
    public List<ReadableImage> toReadable(Product product) {
        return product.getImages().stream()
                .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(image -> toReadable(product, image))
                .toList();
    }
}
