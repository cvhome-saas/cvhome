package com.asrevo.cvhome.store.core.services.customer.attribute;

import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOption;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionSet;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionValue;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;


public interface CustomerOptionSetService extends SalesManagerEntityService<Long, CustomerOptionSet> {


    void saveOrUpdate(CustomerOptionSet entity) throws ServiceException;


    List<CustomerOptionSet> listByStore(MerchantStore store,
                                        Language language) throws ServiceException;


    List<CustomerOptionSet> listByOption(CustomerOption option,
                                         MerchantStore store) throws ServiceException;


    List<CustomerOptionSet> listByOptionValue(CustomerOptionValue optionValue,
                                              MerchantStore store) throws ServiceException;

}
