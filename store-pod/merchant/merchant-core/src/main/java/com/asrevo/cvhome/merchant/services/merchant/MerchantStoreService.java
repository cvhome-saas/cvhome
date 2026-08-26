package com.asrevo.cvhome.merchant.services.merchant;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface MerchantStoreService extends SalesManagerEntityService<StoreMerchantId, MerchantStore> {

    MerchantStore getByMerchantStoreId(StoreMerchantId code);

    void saveOrUpdate(MerchantStore store);

}
