/**
 *
 */
package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import java.util.List;
import java.util.Optional;

/**
 * @author Umesh Awasthi
 */
public interface FilePut {

	/**
	 * Add file to folder
	 */
	void addFile(final String merchantStoreCode, Optional<String> path, InputContentFile inputStaticContentData)
			throws ServiceException;

	/**
	 * Add files to folder
	 */
	void addFiles(final String merchantStoreCode, Optional<String> path,
			List<InputContentFile> inputStaticContentDataList) throws ServiceException;

}
