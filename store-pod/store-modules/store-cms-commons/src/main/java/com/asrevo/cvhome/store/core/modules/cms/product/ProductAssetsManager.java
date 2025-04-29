package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.store.core.modules.cms.common.AssetsManager;
import java.io.Serializable;

public interface ProductAssetsManager
        extends AssetsManager, ProductImageGet, ProductImagePut, ProductImageRemove, Serializable {}
