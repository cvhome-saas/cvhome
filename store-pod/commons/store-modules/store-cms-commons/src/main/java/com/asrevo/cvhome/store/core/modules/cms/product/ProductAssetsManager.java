package com.asrevo.cvhome.store.core.modules.cms.product;

import java.io.Serializable;

import com.asrevo.cvhome.store.core.modules.cms.common.AssetsManager;

public interface ProductAssetsManager
        extends AssetsManager, ProductImageGet, ProductImagePut, ProductImageRemove, Serializable {

}
