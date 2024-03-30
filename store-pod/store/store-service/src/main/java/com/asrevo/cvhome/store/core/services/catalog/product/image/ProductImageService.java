package com.asrevo.cvhome.store.core.services.catalog.product.image;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;
import java.util.Optional;


public interface ProductImageService extends SalesManagerEntityService<Long, ProductImage> {


    /**
     * Add a ProductImage to the persistence and an entry to the CMS
     *
     * @param product
     * @param productImage
     * @param file
     * @throws ServiceException
     */
    void addProductImage(Product product, ProductImage productImage, ImageContentFile inputImage)
            throws ServiceException;

    /**
     * Get the image ByteArrayOutputStream and content description from CMS
     *
     * @param productImage
     * @return
     * @throws ServiceException
     */
    OutputContentFile getProductImage(ProductImage productImage, ProductImageSize size)
            throws ServiceException;

    /**
     * Returns all Images for a given product
     *
     * @param product
     * @return
     * @throws ServiceException
     */
    List<OutputContentFile> getProductImages(Product product)
            throws ServiceException;

    /**
     * Get a product image by name for a given product id
     *
     * @param imageName
     * @param productId
     * @param store
     * @return
     */
    Optional<ProductImage> getProductImage(Long imageId, Long productId, MerchantStore store);

    void removeProductImage(ProductImage productImage) throws ServiceException;

    ProductImage saveOrUpdate(ProductImage productImage) throws ServiceException;

    /**
     * Returns an image file from required identifier. This method is
     * used by the image servlet
     *
     * @param store
     * @param product
     * @param fileName
     * @param size
     * @return
     * @throws ServiceException
     */
    OutputContentFile getProductImage(String storeCode, String productCode,
                                      String fileName, final ProductImageSize size) throws ServiceException;

    void addProductImages(Product product, List<ProductImage> productImages)
            throws ServiceException;

    void updateProductImage(Product product, ProductImage productImage);

}
