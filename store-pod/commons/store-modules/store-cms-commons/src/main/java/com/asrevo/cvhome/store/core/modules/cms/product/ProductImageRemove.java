package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.common.ImageRemove;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;

public interface ProductImageRemove extends ImageRemove {

    void removeProductImage(CmsProductImage productImage) throws ServiceException;

}
