package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.common.ImageRemove;


public interface ProductImageRemove extends ImageRemove {

    void removeProductImage(ProductImage productImage) throws ServiceException;

    void removeProductImages(Product product) throws ServiceException;

}
