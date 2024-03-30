/**
 *
 */
package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;

import java.util.List;
import java.util.Optional;


/**
 * @author Umesh Awasthi
 */

public interface FilePut {

    /**
     * Add file to folder
     *
     * @param merchantStoreCode
     * @param path
     * @param inputStaticContentData
     * @throws ServiceException
     */
    void addFile(final String merchantStoreCode, Optional<String> path, InputContentFile inputStaticContentData)
            throws ServiceException;

    /**
     * Add files to folder
     *
     * @param merchantStoreCode
     * @param path
     * @param inputStaticContentDataList
     * @throws ServiceException
     */
    void addFiles(final String merchantStoreCode,
                  Optional<String> path, List<InputContentFile> inputStaticContentDataList) throws ServiceException;
}
