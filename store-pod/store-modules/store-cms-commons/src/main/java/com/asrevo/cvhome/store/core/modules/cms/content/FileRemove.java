/**
 *
 */
package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import java.util.Optional;

/**
 * @author Umesh Awasthi
 */
public interface FileRemove {

	void removeFile(String merchantStoreCode, FileContentType staticContentType, String fileName, Optional<String> path)
			throws ServiceException;

	void removeFiles(String merchantStoreCode, Optional<String> path) throws ServiceException;

}
