package com.asrevo.cvhome.checkout.services.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.checkout.entity.customer.CustomerList;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.Optional;

public interface CustomerService extends SalesManagerEntityService<Long, Customer> {

	void saveOrUpdate(Customer customer) throws ServiceException;

	CustomerList getListByStore(StoreMerchantId store, CustomerCriteria criteria);

	Optional<Customer> getByCuaExternalId(String sub);

}
