package com.asrevo.cvhome.store.core.modules.cms.common;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import java.util.List;

public interface ImageGet {

	List<OutputContentFile> getImages(final String merchantStoreCode, FileContentType imageContentType)
			throws ServiceException;

}
