package com.asrevo.cvhome.store.core.modules.cms.common;

import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface ImageRemove {

    void removeImages(final String merchantStoreCode) throws ServiceException;
}
