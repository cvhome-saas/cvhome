package com.asrevo.cvhome.catalog.services.image;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;

import lombok.extern.slf4j.Slf4j;

/**
 * Turns image rows into the urls a browser fetches.
 *
 * <p>
 * A library image stores the asset's path in the bucket, cached from content when it was attached; the url is
 * that path under this environment's {@code com.asrevo.cvhome.cdn.base-path}, composed here. Storing the whole
 * url instead is what let the demo catalogue ship pointing at one developer's MinIO, and it would strand every
 * row the day the CDN moved. An external image is served from wherever it lives and is passed through — as is a
 * stored value that already carries a scheme, which is how rows cached before this keep working.
 * </p>
 *
 * <p>
 * Catalog also used to assemble the path itself out of the store id, the sku and a file name, which meant
 * renaming a product's sku silently broke its pictures. The path comes from content now.
 * </p>
 */
@Slf4j
@Component
public class ImageMapper {

    private final String cdnBasePath;

    public ImageMapper(@Value("${com.asrevo.cvhome.cdn.base-path:}") String cdnBasePath) {
        this.cdnBasePath = trimTrailingSlash(cdnBasePath);
        if (this.cdnBasePath == null) {
            log.warn("""
                    com.asrevo.cvhome.cdn.base-path is not set: product image paths are served as stored, \
                    which a browser can only fetch if something else already puts them on a host.""");
        }
    }

    /**
     * Where a browser fetches this image.
     */
    public String url(ProductImage image) {
        if (image.isExternal()) {
            return image.getProductImageUrl();
        }
        String path = image.getImageUrl();
        if (path == null || path.isBlank() || cdnBasePath == null || hasScheme(path)) {
            return path;
        }
        return String.format("%s/%s", cdnBasePath, path);
    }

    public ReadableImage toReadable(Product product, ProductImage image) {
        ReadableImage readable = new ReadableImage();
        readable.setId(image.getId());
        readable.setMediaAssetId(image.getMediaAssetId());
        readable.setImageType(image.getImageType());
        readable.setOrder(image.getSortOrder());
        readable.setDefaultImage(image.isDefaultImage());
        readable.setAltText(image.getAltText());
        readable.setExternalUrl(image.getProductImageUrl());
        readable.setImageUrl(url(image));
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

    private static boolean hasScheme(String path) {
        return path.startsWith("http://") || path.startsWith("https://") || path.startsWith("//");
    }

    private static String trimTrailingSlash(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            return null;
        }
        return basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
    }

}
