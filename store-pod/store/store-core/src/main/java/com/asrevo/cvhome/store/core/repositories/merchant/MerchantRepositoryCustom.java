package com.asrevo.cvhome.store.core.repositories.merchant;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.entity.common.GenericEntityList;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStoreCriteria;

public interface MerchantRepositoryCustom {

    GenericEntityList<MerchantStore> listByCriteria(MerchantStoreCriteria criteria)
            throws ServiceException;


}
