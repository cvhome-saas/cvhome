package com.asrevo.cvhome.catalog.services.image;

import java.util.List;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.errors.ProductImageAssetUnknownException;
import com.asrevo.cvhome.catalog.errors.ProductImageNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;

/**
 * A product's gallery: references into the content service's media library, in display order.
 *
 * <p>
 * Uploads are not catalog's job any more. The seller puts bytes in the media library and catalog stores the
 * asset ids, which is what lets an image carry alt text and dimensions, be reused across products, and be
 * protected from deletion while something still shows it.
 * </p>
 */
public interface ProductImageService {

    List<ReadableImage> list(StoreMerchantId store, Long productId) throws ProductNotFoundException;

    /**
     * Appends images after the ones the product already has.
     *
     * @throws ProductImageAssetUnknownException  an asset id is not in this store's media library
     * @throws RemoteServiceUnavailableException  content could not be reached — nothing is written, because a row
     *                                            with no url is worse than a failed save
     * @throws RemoteServiceTimeoutException      content did not answer in time
     */
    List<ReadableImage> attach(StoreMerchantId store, Long productId, List<PersistableProductImage> items)
            throws ProductNotFoundException, ProductImageAssetUnknownException, RemoteServiceUnavailableException,
            RemoteServiceTimeoutException;

    /**
     * Replaces the whole gallery: order is the list order, and the item flagged default wins. Sent whole because
     * a reorder that arrives one move at a time leaves gaps and ties the storefront resolves arbitrarily.
     */
    List<ReadableImage> replace(StoreMerchantId store, Long productId, List<PersistableProductImage> items)
            throws ProductNotFoundException, ProductImageAssetUnknownException, RemoteServiceUnavailableException,
            RemoteServiceTimeoutException;

    /**
     * Detaches an image from the product. The asset itself stays in the library.
     */
    void delete(StoreMerchantId store, Long productId, Long imageId) throws ProductImageNotFoundException;

    /**
     * Releases a product's hold on its assets before the product is deleted; the rows go with it.
     */
    void forget(Product product);
}
