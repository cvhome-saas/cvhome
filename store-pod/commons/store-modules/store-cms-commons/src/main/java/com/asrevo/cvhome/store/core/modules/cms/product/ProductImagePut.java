package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.ImageSizeMisconfiguredException;
import com.asrevo.cvhome.store.core.modules.cms.errors.ImageUnreadableException;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;

public interface ProductImagePut {

    /**
     * The two image failures are declared here rather than only on the resizing implementation because this is the
     * signature every caller compiles against; a store that uploads straight to S3 simply never throws them.
     */
    void addProductImage(CmsProductImage productImage, ImageContentFile contentImage)
            throws AssetUploadFailedException, ImageUnreadableException, ImageSizeMisconfiguredException;

}
