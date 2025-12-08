package com.asrevo.cvhome.checkout.services.customer.attribute;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.attribute.CustomerAttribute;
import java.util.List;

public interface CustomerAttributeService {

	List<CustomerAttribute> getByCustomer(StoreMerchantId store, Customer customer);

	void delete(CustomerAttribute attribute);

}
