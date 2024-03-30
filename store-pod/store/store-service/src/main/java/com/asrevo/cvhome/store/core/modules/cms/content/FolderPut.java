/**
 *
 */
package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.exception.ServiceException;

import java.util.Optional;


public interface FolderPut {


    /**
     * Create folder on root or on specific path
     *
     * @param merchantStoreCode
     * @param folderName
     * @param path
     * @throws ServiceException
     */
    void addFolder(final String merchantStoreCode, String folderName, Optional<String> path)
            throws ServiceException;

}
