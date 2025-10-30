package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;

public interface ProductImagePut {

	void addProductImage(CmsProductImage productImage, ImageContentFile contentImage) throws ServiceException;

}
