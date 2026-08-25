/**
 *
 */
package com.asrevo.cvhome.store.core.modules.cms.content;

import java.util.Optional;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;

/**
 * @author Umesh Awasthi
 */
public interface FileRemove {

    void removeFile(String merchantStoreCode, FileContentType staticContentType, String fileName, Optional<String> path)
            throws AssetDeleteFailedException;

    void removeFiles(String merchantStoreCode, Optional<String> path) throws AssetDeleteFailedException;

}
