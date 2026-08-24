package com.asrevo.cvhome.catalog.services.image;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.errors.ProductImageNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductImageNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * A product's images: the files on the store's CDN and the rows that point at them.
 */
public interface ProductImageService {

    List<ReadableImage> list(StoreMerchantId store, Long productId) throws ProductNotFoundException;

    /**
     * Stores the files and adds a row for each, numbered from {@code firstPosition}. The first upload of a product
     * with no default image becomes the default, or the first of these when {@code defaultImage} is asked.
     *
     * @throws ProductImageNotPersistedException a file could not be stored
     */
    void add(StoreMerchantId store, Long productId, MultipartFile[] files, int firstPosition, boolean defaultImage)
            throws ProductNotFoundException, ProductImageNotPersistedException;

    void reorder(StoreMerchantId store, Long productId, Long imageId, int position)
            throws ProductImageNotFoundException;

    void delete(StoreMerchantId store, Long productId, Long imageId) throws ProductImageNotFoundException;

    /**
     * Drops every file of a product from the CDN before the product itself is deleted; the rows go with it.
     */
    void removeFiles(Product product);
}
