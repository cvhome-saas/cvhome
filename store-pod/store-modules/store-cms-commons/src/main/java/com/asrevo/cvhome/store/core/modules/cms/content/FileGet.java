package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import java.util.List;
import java.util.Optional;

/**
 * Methods to retrieve the static content from the CMS
 *
 * @author Carl Samson
 */
public interface FileGet {

    OutputContentFile getFile(
            final String merchantStoreCode,
            Optional<String> path,
            FileContentType fileContentType,
            String contentName)
            throws ServiceException;

    List<String> getFileNames(
            final String merchantStoreCode, Optional<String> path, FileContentType fileContentType)
            throws ServiceException;

    List<OutputContentFile> getFiles(
            final String merchantStoreCode, Optional<String> path, FileContentType fileContentType)
            throws ServiceException;
}
