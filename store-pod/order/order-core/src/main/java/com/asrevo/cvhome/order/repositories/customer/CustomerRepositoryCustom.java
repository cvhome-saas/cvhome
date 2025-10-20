package com.asrevo.cvhome.order.repositories.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.order.entity.customer.CustomerList;

public interface CustomerRepositoryCustom {

	CustomerList listByStore(StoreMerchantId store, CustomerCriteria criteria);

}
