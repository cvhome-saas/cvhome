package com.asrevo.cvhome.catalog.services.image;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;

/**
 * Turns image rows into the urls a browser fetches.
 *
 * <p>
 * There is no url building left to do: a library image carries the asset's public URL, cached when it was
 * attached, and an external image is served from wherever it lives. Catalog used to assemble a CDN path out of
 * the store id, the sku and a file name, which meant renaming a product's sku silently broke its pictures.
 * </p>
 */
@Component
public class ImageMapper {

    public ReadableImage toReadable(Product product, ProductImage image) {
        ReadableImage readable = new ReadableImage();
        readable.setId(image.getId());
        readable.setMediaAssetId(image.getMediaAssetId());
        readable.setImageType(image.getImageType());
        readable.setOrder(image.getSortOrder());
        readable.setDefaultImage(image.isDefaultImage());
        readable.setAltText(image.getAltText());
        readable.setExternalUrl(image.getProductImageUrl());
        readable.setImageUrl(image.resolvedUrl());
        if (image.isExternal()) {
            readable.setVideoUrl(image.getProductImageUrl());
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
