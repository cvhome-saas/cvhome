package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.common.ImageGet;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;

import java.util.List;

public interface ProductImageGet extends ImageGet {

    /**
     * Used for accessing the path directly
     *
     * @param merchantStoreCode
     * @param product
     * @param imageName
     * @return
     * @throws ServiceException
     */
    OutputContentFile getProductImage(final String merchantStoreCode, final String productCode,
                                      final String imageName) throws ServiceException;

    OutputContentFile getProductImage(final String merchantStoreCode, final String productCode,
                                      final String imageName, final ProductImageSize size) throws ServiceException;

    OutputContentFile getProductImage(ProductImage productImage) throws ServiceException;

    List<OutputContentFile> getImages(Product product) throws ServiceException;


}
