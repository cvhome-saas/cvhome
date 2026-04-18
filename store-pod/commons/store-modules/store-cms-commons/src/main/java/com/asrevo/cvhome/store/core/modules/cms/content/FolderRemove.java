/**
 *
 */
package com.asrevo.cvhome.store.core.modules.cms.content;

import java.util.Optional;

public interface FolderRemove {

    void removeFolder(final String merchantStoreCode, String folderName, Optional<String> folderPath);

}
