package com.asrevo.cvhome.store.core.modules.cms.content;

import java.util.List;
import java.util.Optional;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetListFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetNotFoundException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetReadFailedException;

/**
 * Methods to retrieve the static content from the CMS
 *
 * @author Carl Samson
 */
public interface FileGet {

    OutputContentFile getFile(String merchantStoreCode, Optional<String> path, FileContentType fileContentType,
                              String contentName) throws AssetNotFoundException, AssetReadFailedException;

    List<String> getFileNames(String merchantStoreCode, Optional<String> path, FileContentType fileContentType)
            throws AssetListFailedException;

    List<OutputContentFile> getFiles(String merchantStoreCode, Optional<String> path,
                                     FileContentType fileContentType) throws AssetListFailedException;

}
