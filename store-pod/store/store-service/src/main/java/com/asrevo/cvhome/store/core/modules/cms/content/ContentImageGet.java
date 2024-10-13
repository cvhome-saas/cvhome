package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.common.ImageGet;
import java.util.List;

public interface ContentImageGet extends ImageGet {

    OutputContentFile getImage(
            final String merchantStoreCode, String imageName, FileContentType imageContentType)
            throws ServiceException;

    List<String> getImageNames(final String merchantStoreCode, FileContentType imageContentType)
            throws ServiceException;
}
