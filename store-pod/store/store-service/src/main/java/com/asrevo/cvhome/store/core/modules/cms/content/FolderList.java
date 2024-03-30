package com.asrevo.cvhome.store.core.modules.cms.content;

import com.asrevo.cvhome.store.core.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface FolderList {

    List<String> listFolders(final String merchantStoreCode, Optional<String> path)
            throws ServiceException;

}
