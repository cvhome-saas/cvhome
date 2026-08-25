package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.common.ImageGet;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetNotFoundException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetReadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;

public interface ProductImageGet extends ImageGet {

    /**
     * Used for accessing the path directly
     */
    OutputContentFile getProductImage(String merchantStoreCode, String productCode, String imageName)
            throws AssetNotFoundException, AssetReadFailedException;

    OutputContentFile getProductImage(String merchantStoreCode, String productCode, String imageName,
                                      ProductImageSize size) throws AssetNotFoundException, AssetReadFailedException;

    OutputContentFile getProductImage(CmsProductImage productImage)
            throws AssetNotFoundException, AssetReadFailedException;

}
