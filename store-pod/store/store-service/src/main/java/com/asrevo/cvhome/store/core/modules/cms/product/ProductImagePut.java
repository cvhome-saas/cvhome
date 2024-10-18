package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface ProductImagePut {

    void addProductImage(ProductImage productImage, ImageContentFile contentImage)
            throws ServiceException;
}
