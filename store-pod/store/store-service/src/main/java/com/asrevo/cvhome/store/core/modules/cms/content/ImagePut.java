package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;

import java.util.List;

public interface ImagePut {


    void addImage(final String merchantStoreCode, InputContentFile image)
            throws ServiceException;

    void addImages(final String merchantStoreCode, List<InputContentFile> imagesList)
            throws ServiceException;

}
