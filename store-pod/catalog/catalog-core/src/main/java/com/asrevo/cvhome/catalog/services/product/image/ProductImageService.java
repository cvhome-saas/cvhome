package com.asrevo.cvhome.catalog.services.product.image;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import java.util.Optional;

public interface ProductImageService extends SalesManagerEntityService<Long, ProductImage> {

    /**
     * Add a ProductImage to the persistence and an entry to the CMS
     */
    void addProductImage(Product product, ProductImage productImage, ImageContentFile inputImage)
            throws ServiceException;

    /**
     * Get the image ByteArrayOutputStream and content description from CMS
     */
    OutputContentFile getProductImage(ProductImage productImage, ProductImageSize size)
            throws ServiceException;

    /**
     * Get a product image by name for a given product id
     */
    Optional<ProductImage> getProductImage(Long imageId, Long productId, StoreMerchantId store);

    void removeProductImage(ProductImage productImage) throws ServiceException;

    ProductImage saveOrUpdate(ProductImage productImage);

    /**
     * Returns an image file from required identifier. This method is
     * used by the image servlet
     */
    OutputContentFile getProductImage(
            String storeCode, String productCode, String fileName, final ProductImageSize size)
            throws ServiceException;

    void addProductImages(Product product, List<ProductImage> productImages)
            throws ServiceException;

    void updateProductImage(Product product, ProductImage productImage);
}
