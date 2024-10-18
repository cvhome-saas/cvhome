package com.asrevo.cvhome.store.core.services.customer.attribute;

import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOption;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

public interface CustomerOptionService extends SalesManagerEntityService<Long, CustomerOption> {

    List<CustomerOption> listByStore(MerchantStore store, Language language)
            throws ServiceException;

    void saveOrUpdate(CustomerOption entity) throws ServiceException;

    CustomerOption getByCode(MerchantStore store, String optionCode);
}
