/**
 *
 */
package com.asrevo.cvhome.store.core.modules.cms.content;

import java.util.List;
import java.util.Optional;

import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;

/**
 * @author Umesh Awasthi
 */
public interface FilePut {

    /**
     * Add file to folder
     */
    void addFile(String merchantStoreCode, Optional<String> path, InputContentFile inputStaticContentData)
            throws AssetUploadFailedException;

    /**
     * Add files to folder
     */
    void addFiles(String merchantStoreCode, Optional<String> path,
                  List<InputContentFile> inputStaticContentDataList) throws AssetUploadFailedException;

}
