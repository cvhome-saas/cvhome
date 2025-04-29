package com.asrevo.cvhome.order.services.customer.attribute;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.customer.attribute.CustomerAttribute;
import java.util.List;

public interface CustomerAttributeService {

    List<CustomerAttribute> getByCustomer(StoreMerchantId store, Customer customer);

    void delete(CustomerAttribute attribute);
}
