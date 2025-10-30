package com.asrevo.cvhome.order.services.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.order.entity.customer.CustomerList;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface CustomerService extends SalesManagerEntityService<Long, Customer> {

	void saveOrUpdate(Customer customer) throws ServiceException;

	CustomerList getListByStore(StoreMerchantId store, CustomerCriteria criteria);

	Customer getByNick(String nick, StoreMerchantId storeMerchantId);

}
