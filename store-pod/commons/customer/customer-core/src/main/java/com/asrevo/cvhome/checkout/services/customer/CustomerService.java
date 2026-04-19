package com.asrevo.cvhome.checkout.services.customer;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface CustomerService extends SalesManagerEntityService<Long, Customer> {

    void saveOrUpdate(Customer customer) throws ServiceException;

    Page<Customer> getListByStore(StoreMerchantId store, CustomerCriteria criteria);

    Optional<Customer> getByCuaExternalId(String sub);

}
