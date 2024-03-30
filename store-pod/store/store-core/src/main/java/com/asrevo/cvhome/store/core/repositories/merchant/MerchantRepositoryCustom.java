package com.asrevo.cvhome.store.core.repositories.merchant;

import com.asrevo.cvhome.store.core.entity.common.GenericEntityList;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStoreCriteria;
import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface MerchantRepositoryCustom {

    GenericEntityList<MerchantStore> listByCriteria(MerchantStoreCriteria criteria)
            throws ServiceException;


}
