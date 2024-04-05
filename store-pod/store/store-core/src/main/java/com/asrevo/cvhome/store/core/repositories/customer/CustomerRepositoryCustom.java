package com.asrevo.cvhome.store.core.repositories.customer;

import com.asrevo.cvhome.store.core.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.store.core.entity.customer.CustomerList;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;


public interface CustomerRepositoryCustom {

    CustomerList listByStore(MerchantStore store, CustomerCriteria criteria);


}
