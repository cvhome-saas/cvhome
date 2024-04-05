package com.asrevo.cvhome.store.core.services.customer.attribute;

import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionValue;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;


public interface CustomerOptionValueService extends SalesManagerEntityService<Long, CustomerOptionValue> {


    List<CustomerOptionValue> listByStore(MerchantStore store, Language language)
            throws ServiceException;

    void saveOrUpdate(CustomerOptionValue entity) throws ServiceException;

    CustomerOptionValue getByCode(MerchantStore store, String optionValueCode);


}
