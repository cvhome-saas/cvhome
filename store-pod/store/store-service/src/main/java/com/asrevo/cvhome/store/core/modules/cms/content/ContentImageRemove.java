package com.asrevo.cvhome.store.core.modules.cms.content;


import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.common.ImageRemove;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;

public interface ContentImageRemove extends ImageRemove {


    void removeImage(final String merchantStoreCode, final FileContentType imageContentType,
                     final String imageName) throws ServiceException;

}
