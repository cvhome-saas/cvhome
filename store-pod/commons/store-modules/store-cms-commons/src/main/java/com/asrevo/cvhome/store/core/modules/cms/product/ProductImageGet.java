package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.common.ImageGet;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;

public interface ProductImageGet extends ImageGet {

    /**
     * Used for accessing the path directly
     */
    OutputContentFile getProductImage(final String merchantStoreCode, final String productCode, final String imageName)
            throws ServiceException;

    OutputContentFile getProductImage(final String merchantStoreCode, final String productCode, final String imageName,
                                      final ProductImageSize size) throws ServiceException;

    OutputContentFile getProductImage(CmsProductImage productImage) throws ServiceException;

}
