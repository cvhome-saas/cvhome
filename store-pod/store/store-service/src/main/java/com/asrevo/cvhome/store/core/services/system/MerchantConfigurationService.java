package com.asrevo.cvhome.store.core.services.system;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfig;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfiguration;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfigurationType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;

public interface MerchantConfigurationService extends
        SalesManagerEntityService<Long, MerchantConfiguration> {

    MerchantConfiguration getMerchantConfiguration(String key, MerchantStore store) throws ServiceException;

    void saveOrUpdate(MerchantConfiguration entity) throws ServiceException;

    List<MerchantConfiguration> listByStore(MerchantStore store)
            throws ServiceException;

    List<MerchantConfiguration> listByType(MerchantConfigurationType type,
                                           MerchantStore store) throws ServiceException;

    MerchantConfig getMerchantConfig(MerchantStore store)
            throws ServiceException;

    void saveMerchantConfig(MerchantConfig config, MerchantStore store)
            throws ServiceException;

}
