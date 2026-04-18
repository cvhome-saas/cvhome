package com.asrevo.cvhome.checkout.repositories.customer;

import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.checkout.entity.customer.CustomerList;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface CustomerRepositoryCustom {

    CustomerList listByStore(StoreMerchantId store, CustomerCriteria criteria);

}
