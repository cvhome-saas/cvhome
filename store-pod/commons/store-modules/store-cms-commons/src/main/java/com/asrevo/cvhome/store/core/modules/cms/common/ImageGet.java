package com.asrevo.cvhome.store.core.modules.cms.common;

import java.util.List;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface ImageGet {

    List<OutputContentFile> getImages(String merchantStoreCode, FileContentType imageContentType)
            throws ServiceException;

}
