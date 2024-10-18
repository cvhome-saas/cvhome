package com.asrevo.cvhome.store.core.services.customer.attribute;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

public interface CustomerAttributeService
        extends SalesManagerEntityService<Long, CustomerAttribute> {

    void saveOrUpdate(CustomerAttribute customerAttribute) throws ServiceException;

    CustomerAttribute getByCustomerOptionId(MerchantStore store, Long customerId, Long id);

    List<CustomerAttribute> getByCustomerOptionValueId(MerchantStore store, Long id);

    List<CustomerAttribute> getByOptionId(MerchantStore store, Long id);

    List<CustomerAttribute> getByCustomer(MerchantStore store, Customer customer);
}
