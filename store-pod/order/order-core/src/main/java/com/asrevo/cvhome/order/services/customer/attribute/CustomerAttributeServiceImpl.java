package com.asrevo.cvhome.order.services.customer.attribute;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.order.repositories.customer.attribute.CustomerAttributeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("customerAttributeService")
public class CustomerAttributeServiceImpl implements CustomerAttributeService {

	private final CustomerAttributeRepository customerAttributeRepository;

	@Autowired
	public CustomerAttributeServiceImpl(CustomerAttributeRepository customerAttributeRepository) {
		this.customerAttributeRepository = customerAttributeRepository;
	}

	@Override
	public List<CustomerAttribute> getByCustomer(StoreMerchantId store, Customer customer) {
		return customerAttributeRepository.findByCustomerId(store, customer.getId());
	}

	@Override
	public void delete(CustomerAttribute attribute) {
		customerAttributeRepository.delete(attribute);
	}

}
