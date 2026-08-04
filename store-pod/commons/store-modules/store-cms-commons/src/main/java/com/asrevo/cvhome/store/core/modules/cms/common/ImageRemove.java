package com.asrevo.cvhome.store.core.modules.cms.common;

import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;

public interface ImageRemove {

    void removeImages(String merchantStoreCode) throws AssetDeleteFailedException;

}
